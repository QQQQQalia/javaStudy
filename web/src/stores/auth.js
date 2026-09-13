import { computed, reactive } from 'vue'

const SESSION_KEY = 'user-center:session'
const REMEMBER_KEY = 'user-center:username'

function readSession() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY) || 'null') || {}
  } catch {
    return {}
  }
}

const saved = readSession()

export const auth = reactive({
  token: saved.token || '',
  id: saved.id || '',
  username: saved.username || '',
  // 上次登录记住的用户名，登录页默认填上
  rememberedUsername: localStorage.getItem(REMEMBER_KEY) || '',
})

export const isLoggedIn = computed(() => Boolean(auth.token))

export function setSession({ token, id, username }) {
  auth.token = token || ''
  auth.id = id || ''
  auth.username = username || ''
  localStorage.setItem(
    SESSION_KEY,
    JSON.stringify({ token: auth.token, id: auth.id, username: auth.username }),
  )
}

export function clearSession() {
  auth.token = ''
  auth.id = ''
  auth.username = ''
  localStorage.removeItem(SESSION_KEY)
}

export function rememberUsername(username) {
  auth.rememberedUsername = username || ''
  if (username) {
    localStorage.setItem(REMEMBER_KEY, username)
  } else {
    localStorage.removeItem(REMEMBER_KEY)
  }
}

/** Vite 代理或后端 token 过期后用来算“还能用多久” */
export function tokenExpireAt(token = auth.token) {
  try {
    const payload = token.split('.')[1]
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/').padEnd(Math.ceil(payload.length / 4) * 4, '='))
    const exp = JSON.parse(json).exp
    return exp ? new Date(exp * 1000) : null
  } catch {
    return null
  }
}
