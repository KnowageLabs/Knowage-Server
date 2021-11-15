const routes = [
    {
        path: '/document-execution/registry/:id',
        name: 'document-execution-registry',
        component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
        props: true
    }
]

export default routes
