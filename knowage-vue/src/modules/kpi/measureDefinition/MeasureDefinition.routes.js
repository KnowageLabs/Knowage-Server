const routes = [
    {
        path: '/measure-definition',
        name: 'measure-definition',
        component: () => import('@/modules/kpi/measureDefinition/MeasureDefinition.vue')
    },
    {
        path: '/measure-definition/new-measure-definition',
        name: 'new-measure-definition',
        component: () => import('@/modules/kpi/measureDefinition/MeasureDefinitionTabView.vue')
    },
    {
        path: '/measure-definition/edit',
        name: 'edit-measure-definition',
        props: (route) => ({ id: route.query.id, ruleVersion: route.query.ruleVersion, clone: route.query.clone }),
        component: () => import('@/modules/kpi/measureDefinition/MeasureDefinitionTabView.vue')
    }
]

export default routes
