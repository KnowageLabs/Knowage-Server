const routes = [
    {
        path: '/configuration-management',
        name: 'configuration-management',
        component: () => import('@/modules/managers/configurationManagement/ConfigurationManagement.vue')
    }
]

export default routes
