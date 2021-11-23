let routes = [
    {
        path: '/dataset-management',
        name: 'dataset-management',
        component: () => import('@/modules/managers/datasetManagement/DatasetManagement.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/datasetManagement/DatasetManagementHint.vue')
            },
            {
                path: 'new-dataset',
                name: 'new-dataset',
                component: () => import('@/modules/managers/datasetManagement/detailView/DatasetManagementDetailView.vue')
            },
            {
                path: ':id',
                name: 'edit-dataset',
                component: () => import('@/modules/managers/datasetManagement/detailView/DatasetManagementDetailView.vue'),
                props: true
            }
        ]
    }
]

export default routes
