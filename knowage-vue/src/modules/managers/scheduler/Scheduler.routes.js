const routes = [
    {
        path: '/scheduler',
        name: 'scheduler',
        component: () => import('@/modules/managers/scheduler/Scheduler.vue')
    }
]

export default routes
