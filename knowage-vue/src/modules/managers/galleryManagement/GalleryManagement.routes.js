let routes = [
    {
        path: '/gallery-management',
        name: 'gallery-management',
        component: () => import('@/modules/managers/galleryManagement/GalleryManagement.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/galleryManagement/GalleryManagementHint.vue')
            },
            {
                path: 'new-template',
                name: 'gallery-detail-new',
                component: () => import('@/modules/managers/galleryManagement/GalleryManagementDetail.vue')
            },
            {
                path: ':id',
                name: 'gallery-detail',
                component: () => import('@/modules/managers/galleryManagement/GalleryManagementDetail.vue'),
                props: true
            }
        ]
    }
]

export default routes
