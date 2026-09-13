import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '../stores/auth.js'
import DevConsoleView from '../views/DevConsoleView.vue'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
  { path: '/register', name: 'register', component: RegisterView, meta: { guestOnly: true } },
  { path: '/home', name: 'home', component: HomeView, meta: { requiresAuth: true } },
  { path: '/dev', name: 'dev', component: DevConsoleView },
  { path: '/:pathMatch(.*)*', redirect: '/home' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  // 需要登录的页面：没 token 就先去登录页，登录后再回到原地址
  if (to.meta.requiresAuth && !isLoggedIn.value) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  // 已经登录了就别再停在登录/注册页
  if (to.meta.guestOnly && isLoggedIn.value) {
    return { name: 'home' }
  }
  return true
})

export default router
