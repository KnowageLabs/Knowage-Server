let routes = [
	{
		path: '/resource-management',
		name: 'resource-management',
		component: () => import('@/modules/managers/resourceManagement/ResourceManagement.vue'),
		meta: { requiresEnterprise: true }
	},
	{
		path: '/models-management',
		name: 'models-management',

		component: () => import('@/modules/managers/resourceManagement/ResourceManagement.vue')
	}
]
export default routes
