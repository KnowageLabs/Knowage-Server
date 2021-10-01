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
                path: 'new-job-schedule',
                name: 'new-job-schedule',
                component: () => import('@/modules/managers/scheduler/SchedulerDetail.vue')
            },
            {
                path: 'edit-job-schedule',
                name: 'edit-job-schedule',
                props: (route) => ({ id: route.query.id, clone: route.query.clone }),
                component: () => import('@/modules/managers/scheduler/SchedulerDetail.vue')
            }
        ]
    }
]

export default routes
