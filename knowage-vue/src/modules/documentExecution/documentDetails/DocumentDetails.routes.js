const routes = [
    {
        path: '/document-details',
        name: 'document-details',
        component: () => import('@/modules/documentExecution/documentDetails/DocumentDetails.vue'),
        children: [
            {
                path: 'new/:folderId',
                component: () => import('@/modules/documentExecution/documentDetails/DocumentDetails.vue'),
                props: true,
                name: 'document-details-new-document'
            },
            {
                path: ':docId',
                component: () => import('@/modules/documentExecution/documentDetails/DocumentDetails.vue'),
                props: true,
                name: 'document-details-edit-document'
            }
        ]
    }
]

export default routes
