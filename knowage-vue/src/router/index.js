import { createRouter, createWebHistory } from 'vue-router'
import IframeRenderer from '../views/IframeRenderer.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/About.vue')
  },
  {
    path: '/knowage/servlet/:catchAll(.*)',
    name: 'knowageUrl',
    component: IframeRenderer,
    props: route => ({ url: route.fullPath })
  },
  {
    path: '/knowage/restful-services/publish:catchAll(.*)',
    component: IframeRenderer,
    props: route => ({ url: route.fullPath })
  }
]

const router = createRouter({
  base: '/knowage/',
  history: createWebHistory(),
  routes
})

export default router
