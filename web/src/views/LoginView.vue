<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { request } from '../api.js'
import { auth, rememberUsername, setSession } from '../stores/auth.js'

const route = useRoute()
const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  code: '',
  remember: true,
})

const captcha = ref('')
const loading = ref(false)
const captchaLoading = ref(false)
const showPassword = ref(false)
const error = ref('')
const notice = ref('')

async function loadCaptcha() {
  captchaLoading.value = true
  const res = await request('GET', '/user/getCode')
  captchaLoading.value = false
  if (res.ok && typeof res.data === 'string') {
    captcha.value = res.data
  } else {
    captcha.value = ''
    error.value = '验证码加载失败，请确认后端已启动'
  }
}

onMounted(() => {
  // 优先用注册页带过来的用户名，其次用上次登录记住的用户名
  const fromQuery = typeof route.query.username === 'string' ? route.query.username : ''
  form.username = fromQuery || auth.rememberedUsername || ''
  if (route.query.registered === '1') {
    notice.value = '注册成功，请使用新账号登录'
  }
  loadCaptcha()
})

async function submit() {
  error.value = ''
  notice.value = ''
  if (!form.username.trim()) {
    error.value = '请输入用户名'
    return
  }
  if (!form.password) {
    error.value = '请输入密码'
    return
  }
  if (!form.code.trim()) {
    error.value = '请输入验证码'
    return
  }

  loading.value = true
  const res = await request('POST', '/user/login', {
    body: {
      username: form.username.trim(),
      password: form.password,
      code: form.code.trim(),
    },
  })
  loading.value = false

  if (res.status === 0) {
    error.value = '连不上后端服务，请确认已启动（默认 3000 端口）'
  } else if (res.data?.success) {
    const user = res.data.data || {}
    setSession({ token: user.token, id: user.id, username: user.username })
    rememberUsername(form.remember ? form.username.trim() : '')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/home'
    router.replace(redirect)
    return
  } else {
    error.value = res.data?.msg || '登录失败，请重试'
  }

  // 验证码一次性使用，失败后换一张新的
  form.code = ''
  loadCaptcha()
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand">
        <div class="brand-logo">UC</div>
        <h1>用户中心</h1>
        <p class="brand-sub">欢迎回来，请登录你的账号</p>
      </div>

      <form class="auth-form" @submit.prevent="submit">
        <div class="field">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="form.username"
            autocomplete="username"
            placeholder="请输入用户名"
          />
        </div>

        <div class="field">
          <label for="password">密码</label>
          <div class="input-affix">
            <input
              id="password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="请输入密码"
            />
            <button
              type="button"
              class="affix-btn"
              @click="showPassword = !showPassword"
              :title="showPassword ? '隐藏密码' : '显示密码'"
            >
              {{ showPassword ? '隐藏' : '显示' }}
            </button>
          </div>
        </div>

        <div class="field">
          <label for="code">验证码</label>
          <div class="captcha-row">
            <input
              id="code"
              v-model="form.code"
              maxlength="6"
              autocomplete="off"
              placeholder="请输入图片中的字符"
            />
            <button
              type="button"
              class="captcha-box"
              :class="{ 'is-loading': captchaLoading }"
              title="点击刷新验证码"
              @click="loadCaptcha"
            >
              <img v-if="captcha" :src="captcha" alt="验证码，点击刷新" />
              <span v-else class="captcha-empty">{{ captchaLoading ? '加载中' : '点击获取' }}</span>
            </button>
          </div>
        </div>

        <div class="row-between">
          <label class="checkbox">
            <input v-model="form.remember" type="checkbox" />
            <span>记住用户名</span>
          </label>
          <button type="button" class="link-btn" @click="loadCaptcha">看不清，换一张</button>
        </div>

        <p v-if="error" class="alert alert-error">{{ error }}</p>
        <p v-else-if="notice" class="alert alert-success">{{ notice }}</p>

        <button class="btn btn-primary btn-block" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>

      <p class="auth-footer">
        还没有账号？
        <router-link :to="{ name: 'register' }">立即注册</router-link>
      </p>
      <p class="auth-tip">
        开发联调：
        <router-link :to="{ name: 'dev' }">接口自检台</router-link>
      </p>
    </div>
  </div>
</template>
