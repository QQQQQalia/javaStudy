import { createApp } from 'vue'
import App from './App.vue'
import { setUnauthorizedHandler } from './api.js'
import router from './router/index.js'
import { clearSession } from './stores/auth.js'
import './style.css'

// token 过期 / 失效时统一清登录态并回登录页
setUnauthorizedHandler(() => {
  clearSession()
  router.replace({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
})

createApp(App).use(router).mount('#app')
