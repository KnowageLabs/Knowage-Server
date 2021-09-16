let routes = [
	{
		path: '/workspace',
		name: 'workspace',
		component: () => import('@/modules/workspace/Workspace.vue'),
		children: [
			{
				path: 'data-preparation',
				name: 'data-preparation',
				component: () => import('@/modules/workspace/dataPreparation/DataPreparation.vue')
			}
		]
	},
	{
		path: '/workspace/data-preparation/:id',
		name: 'data-preparation-detail',
		component: () => import('@/modules/workspace/dataPreparation/DataPreparationDetail.vue'),
		props: true
	}
]

export default routes
