const routes = [
    {
        path: '/timespan',
        name: 'timespan',
        component: () => import('@/modules/managers/timespan/Timespan.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/timespan/TimespanHint.vue')
            },
            {
                path: 'new-timespan',
                name: 'new-timespan',
                component: () => import('@/modules/managers/timespan/TimespanDetail.vue')
            },
            {
                path: 'edit-timespan',
                name: 'edit-timespan',
                props: (route) => ({ id: route.query.id, clone: route.query.clone }),
                component: () => import('@/modules/managers/timespan/TimespanDetail.vue')
            }
        ]
    }
]

export default routes
