let routes = [
    {
        path: '/schemas',
        name: 'schemas',
        component: () => import('@/modules/managers/mondrianSchemasManagement/MondrianSchemasManagement.vue'),
        children: [
            {
                path: 'new-schema',
                name: 'new-schema',
                component: () => import('@/modules/managers/mondrianSchemasManagement/MondrianSchemasManagementTabView.vue')
            },
            {
                path: ':id',
                name: 'edit-schema',
                component: () => import('@/modules/managers/mondrianSchemasManagement/MondrianSchemasManagementTabView.vue'),
                props: true
            }
        ]
    }
]

export default routes
