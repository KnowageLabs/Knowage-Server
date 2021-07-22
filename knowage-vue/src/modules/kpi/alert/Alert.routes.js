let routes = [
    {
        path: '/alert',
        name: 'alert',
        component: () => import('@/modules/kpi/alert/Alert.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/kpi/alert/AlertHint.vue')
            }
        ]
    }
]

export default routes
