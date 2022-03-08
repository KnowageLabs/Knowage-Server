let routes = [
    {
        path: '/layers-management',
        name: 'layers-management',
        component: () => import('@/modules/managers/layersManagement/LayersManagement.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/layersManagement/LayersManagementHint.vue')
            },
            {
                path: 'new-layer',
                name: 'new-layer',
                component: () => import('@/modules/managers/layersManagement/detailView/LayersManagementDetailView.vue')
            },
            {
                path: ':id',
                name: 'edit-layer',
                component: () => import('@/modules/managers/layersManagement/detailView/LayersManagementDetailView.vue'),
                props: true
            }
        ]
    }
]

export default routes
