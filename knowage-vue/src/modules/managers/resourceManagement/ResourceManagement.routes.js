let routes = [
	{
		path: '/resource-management',
		name: 'resource-management',
		component: () => import('@/modules/managers/resourceManagement/ResourceManagement.vue'),

		children: [
			{
				path: '',
				component: () => import('@/modules/managers/resourceManagement/ResourceManagementHint.vue'),
				meta: { requiresEnterprise: true }
			},
			{
				path: ':id',
				name: 'resource-management-detail',
				component: () => import('@/modules/managers/resourceManagement/ResourceManagementDetail.vue'),
				props: true,
				meta: { requiresEnterprise: true }
			}
		]
	},
	{
		path: '/models-management',
		name: 'models-management',

		component: () => import('@/modules/managers/resourceManagement/ResourceManagement.vue'),
		children: [
			{
				path: '',
				component: () => import('@/modules/managers/resourceManagement/ResourceManagementHint.vue')
			},
			{
				path: ':id',
				name: 'resource-management-detail',
				component: () => import('@/modules/managers/resourceManagement/ResourceManagementDetail.vue'),
				props: true
			}
		]
	}
]
export default routes
