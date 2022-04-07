let routes = [
    {
        path: '/data-preparation/:id',
        name: 'data-preparation',
        component: () => import('@/modules/workspace/dataPreparation/DataPreparationDetail.vue'),
        props: (route) => ({ id: route.params.id, transformations: route.params.transformations ? JSON.parse(route.params.transformations) : null, existingProcessId: route.params.processId, existingInstanceId: route.params.instanceId, existingDataset: route.params.dataset })
    }
]

export default routes
