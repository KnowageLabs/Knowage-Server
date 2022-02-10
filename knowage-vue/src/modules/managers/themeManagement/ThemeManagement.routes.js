let routes = [
    {
        path: '/theme-management',
        name: 'theme-management',
        component: () => import('@/modules/managers/themeManagement/ThemeManagement.vue'),
        meta: {
            enterprise: true,
            licenses: ['SI']
        }
    }
]

export default routes
