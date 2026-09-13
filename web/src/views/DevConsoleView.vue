<script setup>
import { reactive, ref } from 'vue'
import { API_BASE, request } from '../api.js'
import { auth } from '../stores/auth.js'

const logs = reactive([])
const session = reactive({ token: auth.token, id: auth.id, username: auth.username })

const registerForm = reactive({ username: 'tom', password: '123456' })
const loginForm = reactive({ username: 'tom', password: '123456', code: '' })
const pwdForm = reactive({ id: auth.id, oldPassword: '123456', newPassword: '654321' })
const delForm = reactive({ id: auth.id })

const captcha = ref('')
const busy = ref(false)

function json(data) {
  return data && typeof data === 'object' ? data : String(data ?? '')
}

async function refreshCaptcha() {
  const res = await request('GET', '/user/getCode', { log: logs })
  if (res.ok && typeof res.data === 'string') captcha.value = res.data
}

async function register() {
  busy.value = true
  const res = await request('POST', '/user/register', { body: { ...registerForm }, log: logs })
  if (res.data?.success) {
    loginForm.username = registerForm.username
    loginForm.password = registerForm.password
  }
  busy.value = false
}

async function login() {
  busy.value = true
  const res = await request('POST', '/user/login', { body: { ...loginForm }, log: logs })
  const vo = res.data?.data
  if (res.data?.success && vo) {
    session.token = vo.token || ''
    session.id = vo.id || ''
    session.username = vo.username || ''
    pwdForm.id = session.id
    delForm.id = session.id
  }
  busy.value = false
  captcha.value = '' // 验证码用过即失效，刷新一张
  loginForm.code = ''
}

async function modifyPassword(withToken = true) {
  busy.value = true
  await request('POST', '/user/modifyPassword', {
    body: { ...pwdForm },
    token: withToken ? session.token : '',
    log: logs,
  })
  busy.value = false
}

async function deleteUser(withToken = true) {
  busy.value = true
  await request('POST', '/user/deleteUser', {
    body: { ...delForm },
    token: withToken ? session.token : '',
    log: logs,
  })
  busy.value = false
}

function logout() {
  session.token = ''
  session.id = ''
  session.username = ''
}
</script>

<template>
  <div class="console-page">
    <p class="console-nav">
      <router-link :to="{ name: auth.token ? 'home' : 'login' }">
        ← 返回{{ auth.token ? '用户中心' : '登录页' }}
      </router-link>
    </p>
    <h1>用户接口联调验证台</h1>
    <p class="sub">
      后端地址：<span class="mono inline-piece">{{ API_BASE }}</span>
      ｜ 三个白名单接口：<code>/user/register</code>、<code>/user/login</code>、<code>/user/getCode</code>；
      其余接口都要在请求头里带 <code>auth: token</code>。
    </p>

    <div class="layout">
      <div>
        <section class="card">
          <h2>1. 注册（不需要 token）</h2>
          <p class="sub">走 /user/register，注册成功后可以拿同一组账号去登录。</p>
          <div class="row">
            <label>用户名</label>
            <input v-model="registerForm.username" placeholder="username" />
          </div>
          <div class="row">
            <label>密码</label>
            <input v-model="registerForm.password" placeholder="password" />
          </div>
          <div class="row">
            <button class="btn btn-primary" :disabled="busy" @click="register">注册</button>
          </div>
        </section>

        <section class="card">
          <h2>2. 获取验证码 + 登录</h2>
          <p class="sub">
            先点图片拿验证码：后端会把验证码文字存进 session，并下发 JSESSIONID。
            登录时浏览器必须带上这个 cookie（本项目所有请求都设了
            <code>credentials: 'include'</code>），否则后端比对不到，会提示「验证码错误」。
          </p>
          <div class="row">
            <label>验证码</label>
            <img
              v-if="captcha"
              class="captcha"
              :src="captcha"
              alt="点击刷新验证码"
              title="点击刷新"
              @click="refreshCaptcha"
            />
            <button class="btn btn-ghost" :disabled="busy" @click="refreshCaptcha">
              {{ captcha ? '换一张' : '获取验证码' }}
            </button>
          </div>
          <div class="row">
            <label>用户名</label>
            <input v-model="loginForm.username" />
          </div>
          <div class="row">
            <label>密码</label>
            <input v-model="loginForm.password" />
          </div>
          <div class="row">
            <label>验证码</label>
            <input v-model="loginForm.code" placeholder="填图片上的字符" @keyup.enter="login" />
          </div>
          <div class="row">
            <button class="btn btn-primary" :disabled="busy" @click="login">登录</button>
            <span v-if="session.token" class="tag">已登录：{{ session.username }}</span>
          </div>
        </section>

        <section class="card">
          <h2>3. 当前登录态</h2>
          <p class="sub">
            登录接口成功后把 data.token 存下来，后面受保护接口用得上。没登录也没关系，
            下面两个接口可以手动指定 id。
          </p>
          <div class="row">
            <label>用户 id</label>
            <input v-model="session.id" placeholder="登录后自动填充" />
          </div>
          <div class="row">
            <label>token</label>
            <input v-model="session.token" placeholder="登录后自动填充" />
            <button class="btn btn-ghost" @click="logout">清空</button>
          </div>
        </section>
      </div>

      <div>
        <section class="card">
          <h2>4. 修改密码（需要 token）</h2>
          <div class="row">
            <label>用户 id</label>
            <input v-model="pwdForm.id" />
          </div>
          <div class="row">
            <label>旧密码</label>
            <input v-model="pwdForm.oldPassword" />
          </div>
          <div class="row">
            <label>新密码</label>
            <input v-model="pwdForm.newPassword" />
          </div>
          <div class="row">
            <button class="btn btn-primary" :disabled="busy" @click="modifyPassword(true)">
              带 token 修改
            </button>
            <button class="btn btn-ghost" :disabled="busy" @click="modifyPassword(false)">
              不带 token 试一次
            </button>
          </div>
          <p class="sub">
            「不带 token」应该返回 401，用来确认拦截器真的生效了。注意：不带 token
            被拦时不代表本次改动有问题。
          </p>
        </section>

        <section class="card">
          <h2>5. 删除用户（需要 token）</h2>
          <div class="row">
            <label>用户 id</label>
            <input v-model="delForm.id" />
          </div>
          <div class="row">
            <button class="btn btn-primary" :disabled="busy" @click="deleteUser(true)">
              带 token 删除
            </button>
            <button class="btn btn-ghost" :disabled="busy" @click="deleteUser(false)">
              不带 token 试一次
            </button>
          </div>
          <p class="sub">
            删除是不可逆的，建议先用一个测试账号验证。删除后用同一个账号再登录一次，
            会看到「用户不存在」。
          </p>
        </section>

        <section class="card">
          <h2>请求记录</h2>
          <p class="sub">每次请求的 method / 地址 / 状态码 / 响应都会记录在这里，方便对照。</p>
          <p v-if="logs.length === 0" class="sub">还没有请求。</p>
          <div v-for="item in logs" :key="item.id" class="log-item">
            <div class="log-head">
              <span class="tag">{{ item.method }}</span>
              <span>{{ item.path }}</span>
              <span
                class="status"
                :class="{ ok: item.status >= 200 && item.status < 300, bad: item.status >= 400 }"
              >
                {{ item.status || 'ERR' }}
              </span>
              <span class="sub inline-piece">{{ item.ms }} ms · {{ item.time }}</span>
            </div>
            <div class="sub log-meta">auth 头：{{ item.token }}</div>
            <div v-if="item.note" class="sub log-meta note">{{ item.note }}</div>
            <div class="mono">{{ json(item.body) }}</div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>
