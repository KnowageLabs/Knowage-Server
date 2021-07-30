const routes = [
    {
        path: '/business-model-catalogue',
        name: 'business-model-catalogue',
        component: () => import('@/modules/managers/businessModelCatalogue/BusinessModelCatalogue.vue'),
        children: [
            {
                path: 'new-business-model',
                name: 'new-business-model',
                component: () => import('@/modules/managers/businessModelCatalogue/BusinessModelCatalogueDetail.vue')
            },
            {
                path: ':id',
                name: 'edit-business-model',
                component: () => import('@/modules/managers/businessModelCatalogue/BusinessModelCatalogueDetail.vue'),
                props: true
            }
        ]
    }
]

export default routes
