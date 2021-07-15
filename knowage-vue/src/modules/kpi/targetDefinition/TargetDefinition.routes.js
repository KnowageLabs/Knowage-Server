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
                path: '/target-definition/edit',
                name: 'edit-target-definition',
                props: (route) => ({ id: route.query.id, clone: route.query.clone }),
                component: () => import('@/modules/kpi/targetDefinition/TargetDefinitionDetail.vue')
            }
        ]
    }
]

export default routes

// {
//     path: '/measure-definition/edit',
//     name: 'edit-measure-definition',
//     props: (route) => ({ id: route.query.id, ruleVersion: route.query.ruleVersion, clone: route.query.clone }),
//     component: () => import('@/modules/kpi/measureDefinition/MeasureDefinitionTabView.vue')
// }
