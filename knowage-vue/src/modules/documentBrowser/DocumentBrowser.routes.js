const routes = [
    {
        path: '/document-browser',
        name: 'document-browser',
        component: () => import('@/modules/documentBrowser/DocumentBrowser.vue'),
        children: [
            {
                path: 'new-dashboard',
                name: 'new-dashboard',
                component: () => import('@/modules/documentBrowser/DocumentExecutionConatiner.vue'),
                props: true
            },
            {
                path: ':mode(registry|document-composite|report|office-doc|olap|map|report|kpi|dossier|etl)/:id',
                name: 'document-browser-document-execution',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            }
        ]
    }
]

export default routes
