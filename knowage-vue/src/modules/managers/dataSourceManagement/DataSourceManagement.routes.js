let routes = [
    {
        path: '/datasource-management',
        name: 'datasource-management',
        component: () => import('@/modules/managers/dataSourceManagement/DataSourceManagement.vue'),
        children: [
            {
                path: '',
                name: 'datasource-hint',
                component: () => import('@/modules/managers/dataSourceManagement/DataSourceManagementHint.vue')
            },
            {
                path: 'new-datasource',
                name: 'new-datasource',
                component: () => import('@/modules/managers/dataSourceManagement/DataSourceTabView/DataSourceDetail.vue')
            },
            {
                path: ':id',
                name: 'edit-datasource',
                component: () => import('@/modules/managers/dataSourceManagement/DataSourceTabView/DataSourceDetail.vue'),
                props: true
            }
        ]
    }
]

export default routes
