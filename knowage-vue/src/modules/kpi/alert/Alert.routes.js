let routes = [
    {
        path: '/alert',
        name: 'alert',
        component: () => import('@/modules/kpi/alert/Alert.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/kpi/alert/AlertHint.vue')
            },
            {
                path: 'new-alert',
                name: 'new-alert',
                component: () => import('@/modules/kpi/alert/AlertDetail.vue')
            },
            {
                path: ':id',
                name: 'edit-alert',
                props: true,
                component: () => import('@/modules/kpi/alert/AlertDetail.vue')
            }
        ]
    }
]

export default routes
