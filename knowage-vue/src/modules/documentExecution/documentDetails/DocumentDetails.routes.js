const routes = [
    {
        path: '/document-details',
        name: 'document-details',
        component: () => import('@/modules/documentExecution/documentDetails/DocumentDetails.vue'),
        children: [
            {
                path: 'new/:folderId',
                component: () => import('@/modules/documentExecution/documentDetails/DocumentDetails.vue'),
                props: true
            },
            {
                path: ':docId',
                component: () => import('@/modules/documentExecution/documentDetails/DocumentDetails.vue'),
                props: true
            }
        ]
    }
]

export default routes
