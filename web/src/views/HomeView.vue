<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiCall } from '../api.js'
import { auth, clearSession, tokenExpireAt } from '../stores/auth.js'

const router = useRouter()

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const pwdLoading = ref(false)
const pwdError = ref('')
const pwdSuccess = ref('')

const confirmingDelete = ref(false)
const deleteLoading = ref(false)
const deleteError = ref('')

const expireAt = computed(() => tokenExpireAt())
const expireText = computed(() => {
  if (!expireAt.value) return '未知'
  const minutes = Math.round((expireAt.value.getTime() - Date.now()) / 60000)
  if (minutes <= 0) return '已过期'
  return `${expireAt.value.toLocaleString('zh-CN', { hour12: false })}（约 ${minutes} 分钟后过期）`
})

function logout() {
  clearSession()
  router.replace({ name: 'login' })
}

async function changePassword() {
  pwdError.value = ''
  pwdSuccess.value = ''
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    pwdError.value = '请填写旧密码和新密码'
    return
  }
  if (pwdForm.newPassword.length < 6) {
    pwdError.value = '新密码至少 6 位'
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirm) {
    pwdError.value = '两次输入的新密码不一致'
    return
  }

  pwdLoading.value = true
  const res = await apiCall('POST', '/user/modifyPassword', {
    id: auth.id,
    oldPassword: pwdForm.oldPassword,
    newPassword: pwdForm.newPassword,
  })
  pwdLoading.value = false

  if (res.status === 0) {
    pwdError.value = '连不上后端服务'
    return
  }
  if (res.status === 401) {
    pwdError.value = '登录已过期，请重新登录'
    return
  }
  if (res.data?.success) {
    pwdSuccess.value = '密码修改成功，下次请用新密码登录'
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirm = ''
  } else {
    pwdError.value = res.data?.msg || '修改失败，请重试'
  }
}

async function deleteAccount() {
  deleteError.value = ''
  deleteLoading.value = true
  const res = await apiCall('POST', '/user/deleteUser', { id: auth.id })
  deleteLoading.value = false

  if (res.data?.success) {
    clearSession()
    router.replace({ name: 'login' })
    return
  }
  deleteError.value = res.status === 401 ? '登录已过期，请重新登录' : res.data?.msg || '注销失败'
  confirmingDelete.value = false
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="header-brand">
        <div class="brand-logo small">UC</div>
        <span>用户中心</span>
      </div>
      <div class="header-right">
        <span class="user-chip">
          <span class="avatar">{{ (auth.username || '?').slice(0, 1).toUpperCase() }}</span>
          {{ auth.username || '未命名用户' }}
        </span>
        <button class="btn btn-ghost" @click="logout">退出登录</button>
      </div>
    </header>

    <main class="app-main">
      <section class="panel">
        <h2>账号信息</h2>
        <dl class="info-grid">
          <dt>用户名</dt>
          <dd>{{ auth.username }}</dd>
          <dt>用户 id</dt>
          <dd class="mono">{{ auth.id }}</dd>
          <dt>登录状态</dt>
          <dd>有效，截止 {{ expireText }}</dd>
        </dl>
      </section>

      <section class="panel">
        <h2>修改密码</h2>
        <p class="sub">修改密码需要带上登录时拿到的 token，请求头里会自动加上 <code>auth</code>。</p>
        <form class="form-grid" @submit.prevent="changePassword">
          <div class="field">
            <label for="old">旧密码</label>
            <input id="old" v-model="pwdForm.oldPassword" type="password" autocomplete="current-password" />
          </div>
          <div class="field">
            <label for="new">新密码</label>
            <input id="new" v-model="pwdForm.newPassword" type="password" autocomplete="new-password" />
          </div>
          <div class="field">
            <label for="confirm">确认新密码</label>
            <input id="confirm" v-model="pwdForm.confirm" type="password" autocomplete="new-password" />
          </div>
          <p v-if="pwdError" class="alert alert-error">{{ pwdError }}</p>
          <p v-if="pwdSuccess" class="alert alert-success">{{ pwdSuccess }}</p>
          <button class="btn btn-primary" type="submit" :disabled="pwdLoading">
            {{ pwdLoading ? '提交中…' : '保存新密码' }}
          </button>
        </form>
      </section>

      <section class="panel danger-panel">
        <h2>注销账号</h2>
        <p class="sub">注销会删除数据库里的这条用户记录，且无法恢复。</p>
        <p v-if="deleteError" class="alert alert-error">{{ deleteError }}</p>
        <button v-if="!confirmingDelete" class="btn btn-danger" @click="confirmingDelete = true">
          注销账号
        </button>
        <div v-else class="row-gap">
          <span class="sub">确定要注销 <strong>{{ auth.username }}</strong> 吗？</span>
          <button class="btn btn-danger" :disabled="deleteLoading" @click="deleteAccount">
            {{ deleteLoading ? '注销中…' : '确认注销' }}
          </button>
          <button class="btn btn-ghost" :disabled="deleteLoading" @click="confirmingDelete = false">
            取消
          </button>
        </div>
      </section>
    </main>
  </div>
</template>
