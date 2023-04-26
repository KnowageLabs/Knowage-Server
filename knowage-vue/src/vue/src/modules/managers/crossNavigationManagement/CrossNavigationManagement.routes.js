const routes = [
    {
        path: '/cross-navigation-management',
        name: 'cross-navigation',
        component: () => import('@/modules/managers/crossNavigationManagement/CrossNavigationManagement.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/crossNavigationManagement/CrossNavigationManagementHint.vue')
            },
            {
                path: 'new-navigation',
                name: 'navigation-detail-new',
                component: () => import('@/modules/managers/crossNavigationManagement/CrossNavigationManagementDetail.vue')
            },
            {
                path: ':id',
                name: 'navigation-detail',
                component: () => import('@/modules/managers/crossNavigationManagement/CrossNavigationManagementDetail.vue'),
                props: true
            }
        ]
    }
]

export default routes
