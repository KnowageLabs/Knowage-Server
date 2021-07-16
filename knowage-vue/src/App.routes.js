import { createRouter, createWebHistory } from 'vue-router'
import IframeRenderer from '@/modules/commons/IframeRenderer.vue'
import managersRoutes from '@/modules/managers/managers.routes.js'
import importExportRoutes from '@/modules/importExport/ImportExport.routes.js'
import kpiRoutes from '@/modules/kpi/kpi.routes.js'

const baseRoutes = [
    {
        path: '/',
        name: 'home',
        component: () => import('@/views/Home.vue')
    },
    {
        path: '/about',
        name: 'about',
        component: () => import('@/views/About.vue')
    },
    {
        path: '/knowage/servlet/:catchAll(.*)',
        name: 'knowageUrl',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/restful-services/publish:catchAll(.*)',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/restful-services/signup:catchAll(.*)',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/themes:catchAll(.*)',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/login',
        name: 'login',
        redirect: process.env.VUE_APP_HOST_URL + '/knowage/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE'
    },
    {
        path: '/:catchAll(.*)',
        component: () => import('@/modules/commons/404.vue')
    }
]

const routes = baseRoutes
    .concat(managersRoutes)
    .concat(importExportRoutes)
    .concat(kpiRoutes)

const router = createRouter({
    base: process.env.VUE_APP_PUBLIC_PATH,
    history: createWebHistory(process.env.VUE_APP_PUBLIC_PATH),
    routes
})

/*router.beforeEach((to, from, next) => {
	console.log('to',to)
	console.log('from',from)
	console.log('next',next)
	next()
})*/

export default router
