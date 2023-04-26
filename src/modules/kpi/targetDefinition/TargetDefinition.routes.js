const routes = [
    {
        path: '/target-definition',
        name: 'target-definition',
        component: () => import('@/modules/kpi/targetDefinition/TargetDefinition.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/kpi/targetDefinition/TargetDefinitionHint.vue')
            },
            {
                path: 'new-target-definition',
                name: 'new-target-definition',
                component: () => import('@/modules/kpi/targetDefinition/TargetDefinitionDetail.vue')
            },
            {
                path: '/target-definition/edit',
                name: 'edit-target-definition',
                props: (route) => ({ id: route.query.id, clone: route.query.clone }),
                component: () => import('@/modules/kpi/targetDefinition/TargetDefinitionDetail.vue')
            }
        ]
    }
]

export default routes
