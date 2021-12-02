const routes = [
    {
        path: '/:mode(registry|document-composite|report|office-doc|olap|map|report|kpi|dossier|etl)/:id',
        name: 'document-execution',
        component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
        props: true
    },
    {
        path: '/embed/:mode(registry|document-composite|report|office-doc|olap|map|report|kpi|dossier|etl)/:id',
        name: 'document-execution-embed',
        component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
        props: true
    },
    {
        path: '/workspace/:mode(registry|document-composite|report|office-doc|olap|map|report|kpi|dossier|etl)/:id',
        name: 'document-execution-workspace',
        component: () => import('@/modules/documentExecution/main/DocumentExecution.vue'),
        props: true
    }
]

export default routes
