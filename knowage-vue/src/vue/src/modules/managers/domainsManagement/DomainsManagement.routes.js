const routes = [
    {
        path: '/domains-management',
        name: 'domains-management',
        component: () => import('@/modules/managers/domainsManagement/DomainsManagement.vue')
    }
]

export default routes
