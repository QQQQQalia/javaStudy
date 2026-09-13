<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '../api.js'

const router = useRouter()

const form = reactive({ username: '', password: '', confirm: '' })
const loading = ref(false)
const error = ref('')
const success = ref('')

async function submit() {
  error.value = ''
  success.value = ''

  if (!form.username.trim()) {
    error.value = '请输入用户名'
    return
  }
  if (form.password.length < 6) {
    error.value = '密码至少 6 位'
    return
  }
  if (form.password !== form.confirm) {
    error.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  const res = await request('POST', '/user/register', {
    body: { username: form.username.trim(), password: form.password },
  })
  loading.value = false

  if (res.status === 0) {
    error.value = '连不上后端服务，请确认已启动'
    return
  }
  if (res.data?.success) {
    success.value = '注册成功，正在跳转到登录页…'
    setTimeout(() => {
      router.replace({ name: 'login', query: { username: form.username.trim(), registered: '1' } })
    }, 800)
  } else {
    error.value = res.data?.msg || '注册失败，请重试'
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand">
        <div class="brand-logo">UC</div>
        <h1>注册账号</h1>
        <p class="brand-sub">注册后即可登录用户中心</p>
      </div>

      <form class="auth-form" @submit.prevent="submit">
        <div class="field">
          <label for="reg-username">用户名</label>
          <input id="reg-username" v-model="form.username" autocomplete="username" placeholder="请输入用户名" />
        </div>
        <div class="field">
          <label for="reg-password">密码</label>
          <input
            id="reg-password"
            v-model="form.password"
            type="password"
            autocomplete="new-password"
            placeholder="至少 6 位"
          />
        </div>
        <div class="field">
          <label for="reg-confirm">确认密码</label>
          <input
            id="reg-confirm"
            v-model="form.confirm"
            type="password"
            autocomplete="new-password"
            placeholder="请再输入一次密码"
          />
        </div>

        <p v-if="error" class="alert alert-error">{{ error }}</p>
        <p v-if="success" class="alert alert-success">{{ success }}</p>

        <button class="btn btn-primary btn-block" type="submit" :disabled="loading">
          {{ loading ? '注册中…' : '注册' }}
        </button>
      </form>

      <p class="auth-footer">
        已有账号？
        <router-link :to="{ name: 'login' }">返回登录</router-link>
      </p>
    </div>
  </div>
</template>
