let routes = [
    {
        path: '/import-export',
        name: 'import-export',
        component: () => import('@/modules/importExport/ImportExport.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/importExport/gallery/ImportExportGallery.vue')
            },
            {
                path: 'gallery',
                component: () => import('@/modules/importExport/gallery/ImportExportGallery.vue')
            }
        ]
    }
]

export default routes
