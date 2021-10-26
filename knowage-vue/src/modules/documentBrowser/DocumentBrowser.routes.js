const routes = [
    {
        path: '/document-browser',
        name: 'document-browser',
        component: () => import('@/modules/documentBrowser/DocumentBrowser.vue'),
        children: [
            {
                path: 'document-execution/:id',
                name: 'document-execution',
                component: () => import('@/modules/documentBrowser/DocumentExecutionConatiner.vue'),
                props: true
            }
        ]
    }
]

export default routes
