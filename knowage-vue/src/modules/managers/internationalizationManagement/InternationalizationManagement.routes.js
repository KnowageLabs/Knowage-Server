let routes = [
    {
        path: '/language',
        name: 'language',
        component: () => import('@/modules/managers/internationalizationManagement/InternationalizationManagement.vue')
    }
]

export default routes
