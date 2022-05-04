const routes = [
    {
        path: '/scheduler',
        name: 'scheduler',
        component: () => import('@/modules/managers/scheduler/Scheduler.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/scheduler/SchedulerHint.vue')
            },
            {
                path: 'new-package-schedule',
                name: 'new-package-schedule',
                component: () => import('@/modules/managers/scheduler/SchedulerDetail.vue')
            },
            {
                path: 'edit-package-schedule',
                name: 'edit-package-schedule',
                props: (route) => ({ id: route.query.id, clone: route.query.clone }),
                component: () => import('@/modules/managers/scheduler/SchedulerDetail.vue')
            }
        ]
    }
]

export default routes
