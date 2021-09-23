const routes = [
    {
        path: '/document-execution/registry/:id',
        name: 'registry',
        component: () => import('@/modules/documentExecution/registry/Registry.vue'),
        props: true
    }
]

export default routes
