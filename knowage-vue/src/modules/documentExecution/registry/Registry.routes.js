const routes = [
    {
        path: '/registry',
        name: 'registry',
        component: () => import('@/modules/documentExecution/registry/Registry.vue')
    }
]

export default routes
