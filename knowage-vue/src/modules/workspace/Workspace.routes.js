let routes = [
    {
        path: '/workspace',
        name: 'workspace',
        component: () => import('@/modules/workspace/Workspace.vue'),
        children: [
            {
                path: 'data-preparation',
                name: 'data-preparation',
                component: () => import('@/modules/managers/tenantManagement/TenantManagementTabView/TenantManagementTabView.vue')
            }
        ]
    }
]

export default routes
