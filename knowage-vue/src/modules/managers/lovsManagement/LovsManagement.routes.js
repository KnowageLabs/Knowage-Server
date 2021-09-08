const routes = [
    {
        path: '/lovs-management',
        name: 'lovs-management',
        component: () => import('@/modules/managers/lovsManagement/LovsManagement.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/lovsManagement/LovesManagementHint.vue')
            },
            {
                path: 'new-lov',
                name: 'new-lov',
                component: () => import('@/modules/managers/lovsManagement/LovsManagementDetail.vue')
            },
            {
                path: ':id',
                name: 'edit-lov',
                component: () => import('@/modules/managers/lovsManagement/LovsManagementDetail.vue'),
                props: true
            }
        ]
    }
]

export default routes
