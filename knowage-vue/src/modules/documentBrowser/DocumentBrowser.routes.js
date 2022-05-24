const routes = [
    {
        path: '/document-browser',
        name: 'document-browser',
        component: () => import('@/modules/documentBrowser/DocumentBrowser.vue'),
        children: [
            {
                path: 'new-dashboard',
                name: 'new-dashboard',
                component: () => import('@/modules/documentBrowser/DocumentBrowserCockpitContainer.vue'),
                props: true
            },
            {
                path: 'document-details/new/:folderId',
                name: 'document-browser-document-details-new',
                component: () => import('@/modules/documentBrowser/DocumentBrowserCockpitContainer.vue'),
                props: true
            },
            {
                path: 'document-details/:id',
                name: 'document-browser-document-details-edit',
                component: () => import('@/modules/documentBrowser/DocumentBrowserCockpitContainer.vue'),
                props: true
            },
            {
                path: ':mode(registry|document-composite|report|office-doc|olap|map|report|kpi|dossier|etl)/:id',
                name: 'document-browser-document-execution',
                component: () => import('@/modules/documentBrowser/DocumentBrowserCockpitContainer.vue'),
                props: true
            }
        ]
    }
]

export default routes
