const routes = [
    {
        path: '/kpi-scheduler',
        name: 'kpi-scheduler',
        component: () => import('@/modules/kpi/kpiScheduler/KpiScheduler.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/kpi/kpiScheduler/KpiSchedulerHint.vue')
            },
            {
                path: 'new-kpi-schedule',
                name: 'new-kpi-schedule',
                component: () => import('@/modules/kpi/kpiScheduler/KpiSchedulerTabView.vue')
            },
            {
                path: 'edit-kpi-schedule',
                name: 'edit-kpi-schedule',
                props: (route) => ({ id: route.query.id, ruleVersion: route.query.ruleVersion, clone: route.query.clone }),
                component: () => import('@/modules/kpi/kpiScheduler/KpiSchedulerTabView.vue')
            }
        ]
    }
]

export default routes
