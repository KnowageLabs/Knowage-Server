const routes = [
    {
        path: '/document-browser',
        name: 'document-browser',
        component: () => import('@/modules/documentBrowser/DocumentBrowser.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/documentBrowser/documentBrowserHome/DocumentBrowserHome.vue')
            }
        ]
    }
]

export default routes
