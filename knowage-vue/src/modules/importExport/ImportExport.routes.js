let routes = [
    {
        path: '/knowage/importexport',
        name: 'importexport',
        component: () => import('@/modules/importExport/ImportExport.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/importExport/gallery/ImportExportGallery.vue')
            }
        ]
    }
]

export default routes
