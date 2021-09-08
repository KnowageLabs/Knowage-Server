const routes = [
    {
        path: '/document-execution/registry',
        name: 'registry',
        component: () => import('@/modules/documentExecution/registry/Registry.vue')
    }
]

export default routes
