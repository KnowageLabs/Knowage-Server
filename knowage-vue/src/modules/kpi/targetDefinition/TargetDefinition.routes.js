let routes = [
    {
        path: '/target-definition',
        name: 'target-definition',
        component: () => import('@/modules/kpi/targetDefinition/TargetDefinition.vue'),
        children: [
            {
                path: 'new-target-definition',
                name: 'new-target-definition',
                component: () => import('@/modules/kpi/targetDefinition/TargetDefinitionDetail.vue')
            },
            {
                path: ':id',
                name: 'edit-target-definition',
                props: true,
                component: () => import('@/modules/kpi/targetDefinition/TargetDefinitionDetail.vue')
            }
        ]
    }
]

export default routes
