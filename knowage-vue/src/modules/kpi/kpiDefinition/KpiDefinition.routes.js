let routes = [
    {
        path: '/kpidef',
        name: 'kpi-definition',
        component: () => import('@/modules/kpi/kpiDefinition/KpiDefinition.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/kpi/kpiDefinition/KpiDefinitionHint.vue')
            },
            {
                path: 'new-kpi',
                name: 'new-kpi',
                component: () => import('@/modules/kpi/kpiDefinition/detailView/KpiDefinitionDetail.vue')
            },
            {
                path: ':id/:version',
                name: 'edit-kpi',
                component: () => import('@/modules/kpi/kpiDefinition/detailView/KpiDefinitionDetail.vue'),
                props: true
            }
        ]
    }
]

export default routes
