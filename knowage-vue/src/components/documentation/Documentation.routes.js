const routes = [
    {
        path: `/docs`,
        name: 'docs',
        component: () => import('@/components/documentation/Documentation.vue'),
        children: [
            {
                path: '',
                name: 'docs-index',
                component: () => import('@/components/documentation/DocumentationPage.vue')
            },
            {
                path: ':pathMatch(.*)*',
                name: 'docs-page',
                props: (route) => ({ path: route.params.pathMatch || '', query: route.query }),
                component: () => import('@/components/documentation/DocumentationPage.vue')
            }
        ]
    }
]

export default routes
