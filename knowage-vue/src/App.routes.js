import { createRouter, createWebHistory } from 'vue-router'
import IframeRenderer from '@/modules/shared/IframeRenderer.vue'
import managersRoutes from '@/modules/managers/managers.routes.js'

const baseRoutes = [
	{
		path: '/knowage',
		name: 'home',
		component: () => import('@/views/Home.vue')
	},
	{
		path: '/knowage/about',
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
		path: '/knowagecockpitengine:catchAll(.*)',
		component: IframeRenderer,
		props: (route) => ({ url: route.fullPath })
	},
	{
		path: '/:catchAll(.*)',
		component: () => import('@/modules/shared/404.vue')
	}
]

const routes = baseRoutes.concat(managersRoutes)

const router = createRouter({
	base: '/knowage/',
	history: createWebHistory(),
	routes
})

/*router.beforeEach((to, from, next) => {
	console.log('to',to)
	console.log('from',from)
	console.log('next',next)
	next()
})*/

export default router
