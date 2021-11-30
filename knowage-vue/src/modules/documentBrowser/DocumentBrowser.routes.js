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
                path: 'registry/:id',
                name: 'document-browser-document-execution-registry',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'document-composite/:id',
                name: 'document-browser-document-execution-document-composite',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'office-doc/:id',
                name: 'document-browser-document-execution-office-doc',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'olap/:id',
                name: 'document-browser-document-execution-olap',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'map/:id',
                name: 'document-browser-document-execution-map',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'report/:id',
                name: 'document-browser-document-execution-report',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'kpi/:id',
                name: 'document-browser-document-execution-kpi',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'dossier/:id',
                name: 'document-browser-document-execution-dossier',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'etl/:id',
                name: 'document-browser-document-execution-etl',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            }
        ]
    }
]

export default routes
