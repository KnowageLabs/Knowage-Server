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
                name: 'document-browser-registry',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'document-composite/:id',
                name: 'document-browser-document-composite',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'office-doc/:id',
                name: 'document-browser-office-doc',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'olap/:id',
                name: 'document-browser-olap',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'map/:id',
                name: 'document-browser-map',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'report/:id',
                name: 'document-browser-report',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'kpi/:id',
                name: 'document-browser-kpi',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'dossier/:id',
                name: 'document-browser-dossier',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            },
            {
                path: 'etl/:id',
                name: 'document-browser-etl',
                component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
                props: true
            }
        ]
    }
]

export default routes
