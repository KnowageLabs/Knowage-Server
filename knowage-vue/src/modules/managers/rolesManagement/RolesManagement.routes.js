let routes = [
    {
        path: '/roles',
        name: 'roles',
        component: () => import('@/modules/managers/rolesManagement/RolesManagement.vue'),
        children: [
            {
                path: 'new-role',
                name: 'new-role',
                component: () => import('@/modules/managers/rolesManagement/RolesManagementTabView.vue')
            },
            {
                path: ':id',
                name: 'edit-role',
                component: () => import('@/modules/managers/rolesManagement/RolesManagementTabView.vue'),
                props: true
            }
        ]
    }
]

export default routes
