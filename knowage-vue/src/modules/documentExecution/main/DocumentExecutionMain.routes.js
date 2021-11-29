const routes = [
    {
        path: '/registry/:id',
        name: 'document-execution-registry',
        component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
        props: true
        // children: [
        //     {
        //         path: 'registry/:id',
        //         name: 'document-execution-registry',
        //         component: () => import('@/modules/documentExecution/registry/Registry.vue'),
        //         props: true
        //     }
        // ]
    }
]

export default routes
