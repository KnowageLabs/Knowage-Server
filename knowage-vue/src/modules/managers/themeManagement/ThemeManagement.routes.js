let routes = [
    {
        path: '/theme-management',
        name: 'theme-management',
        component: () => import('@/modules/managers/themeManagement/ThemeManagement.vue'),
        children: [
            {
                path: 'new-theme',
                name: 'new-theme',
                component: () => import('@/modules/managers/tenantManagement/TenantManagementTabView/TenantManagementTabView.vue')
            },
            {
                path: ':id',
                name: 'edit-theme',
                component: () => import('@/modules/managers/tenantManagement/TenantManagementTabView/TenantManagementTabView.vue'),
                props: true
            }
        ]
    }
]

export default routes
