let routes = [
    {
        path: '/tenants-management',
        name: 'tenants-management',
        component: () => import('@/modules/managers/tenantManagement/TenantManagement.vue'),
        children: [
            {
                path: 'new-tenant',
                name: 'new-tenant',
                component: () => import('@/modules/managers/tenantManagement/TenantManagementTabView/TenantManagementTabView.vue')
            },
            {
                path: ':id',
                name: 'edit-tenant',
                component: () => import('@/modules/managers/tenantManagement/TenantManagementTabView/TenantManagementTabView.vue'),
                props: true
            }
        ]
    }
]

export default routes
