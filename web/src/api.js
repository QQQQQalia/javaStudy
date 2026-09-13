import { auth } from './stores/auth.js'

// 后端地址：默认 3000 端口，可用 web/.env.local 里的 VITE_API_BASE 覆盖
export const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:3000'

let onUnauthorized = null

/** 由 main.js 注入：token 失效时清登录态并跳回登录页 */
export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

let seq = 0

/**
 * 统一请求方法：所有请求都带 credentials: 'include'，
 * 因为登录流程依赖 JSESSIONID 这个 cookie（验证码存在 session 里）。
 */
export async function request(method, path, { body, token, log } = {}) {
  const url = API_BASE + path
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.auth = token

  const entry = {
    id: ++seq,
    method,
    path,
    url,
    time: new Date().toLocaleTimeString('zh-CN', { hour12: false }),
    token: token ? `${token.slice(0, 16)}…` : '（无）',
    status: 0,
    ok: false,
    body: '',
    text: '',
    ms: 0,
    note: '',
  }
  log?.unshift(entry)

  const started = performance.now()
  try {
    const res = await fetch(url, {
      method,
      headers,
      credentials: 'include',
      body: body === undefined ? undefined : JSON.stringify(body),
    })
    entry.status = res.status
    entry.ms = Math.round(performance.now() - started)
    entry.text = await res.text()
    entry.ok = res.ok
    try {
      entry.body = JSON.parse(entry.text)
    } catch {
      entry.body = entry.text
    }
    if (res.status === 401) {
      entry.note = '401：被 AuthInterceptor 拦下（没带 token、token 无效或已过期）'
    }
    return { ok: res.ok, status: res.status, data: entry.body }
  } catch (err) {
    entry.ms = Math.round(performance.now() - started)
    entry.note = `请求失败：${err.message}（后端没启动，或被浏览器的 CORS 拦住了）`
    return { ok: false, status: 0, error: err }
  }
}

/**
 * 带登录态的请求：自动带上 auth 头。
 * 返回 401 视为 token 失效，交给上层登出；不带 token 的探测请求不会触发登出。
 */
export async function apiCall(method, path, body) {
  const sentToken = Boolean(auth.token)
  const res = await request(method, path, { body, token: auth.token })
  if (res.status === 401 && sentToken) {
    onUnauthorized?.()
  }
  return res
}
