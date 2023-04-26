const routes = [
    {
        path: '/news-management',
        name: 'news-management',
        component: () => import('@/modules/managers/newsManagement/NewsManagement.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/newsManagement/NewsManagementHint.vue')
            },
            {
                path: 'new-news',
                name: 'new-news',
                component: () => import('@/modules/managers/newsManagement/NewsManagementDetail.vue')
            },
            {
                path: ':id',
                name: 'edit-news',
                component: () => import('@/modules/managers/newsManagement/NewsManagementDetail.vue'),
                props: true
            }
        ]
    }
]

export default routes
