let routes = [
    {
        path: '/datasource',
        name: 'datasource',
        component: () => import('@/modules/managers/dataSourceManagement/DataSourceManagement.vue'),
        children: [
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
