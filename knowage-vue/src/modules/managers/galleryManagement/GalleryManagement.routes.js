let routes = [
    {
        path: '/gallerymanagement',
        name: 'gallerymanagement',
        component: () => import('@/modules/managers/galleryManagement/GalleryManagement.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/galleryManagement/GalleryManagementHint.vue')
            },
            {
                path: 'newtemplate',
                name: 'gallerydetailnew',
                component: () => import('@/modules/managers/galleryManagement/GalleryManagementDetail.vue')
            },
            {
                path: ':id',
                name: 'gallerydetail',
                component: () => import('@/modules/managers/galleryManagement/GalleryManagementDetail.vue'),
                props: true
            }
        ]
    }
]

export default routes
