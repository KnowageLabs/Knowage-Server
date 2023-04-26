const routes = [
    {
        path: '/gis',
        name: 'gis-document-designer',
        component: () => import('@/modules/workspace/gisDocumentDesigner/GisDocumentDesigner.vue'),
        children: [
            {
                path: 'new',
                name: 'new-gis',
                component: () => import('@/modules/workspace/gisDocumentDesigner/GisDocumentDesigner.vue')
            },
            {
                path: 'edit',
                name: 'edit-gis',
                props: (route) => ({ documentId: route.query.id }),
                component: () => import('@/modules/workspace/gisDocumentDesigner/GisDocumentDesigner.vue')
            }
        ]
    }
]

export default routes
