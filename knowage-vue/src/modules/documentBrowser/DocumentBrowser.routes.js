const routes = [
    {
        path: '/document-browser',
        name: 'document-browser',
        component: () => import('@/modules/documentBrowser/DocumentBrowser.vue'),
        children: [
            {
                path: 'document-execution/new-dashboard',
                name: 'new-dashboard',
                component: () => import('@/modules/documentBrowser/DocumentExecutionConatiner.vue'),
                props: true
            },
            {
                path: 'document-execution/:id',
                name: 'document-browser-document-execution',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            }
        ]
    }
]

export default routes
