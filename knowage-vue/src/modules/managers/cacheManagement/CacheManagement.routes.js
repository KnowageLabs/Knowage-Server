const routes = [
    {
        path: '/cache-management',
        name: 'cache-management',
        component: () => import('@/modules/managers/cacheManagement/CacheManagement.vue')
    }
]

export default routes
