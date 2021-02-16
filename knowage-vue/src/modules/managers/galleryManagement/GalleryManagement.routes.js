let routes = [
		{
			path: '/knowage/gallerymanagement',
			name: 'gallerymanagement',
			component: () => import('@/modules/managers/galleryManagement/GalleryManagement.vue'),
			children: [
				{
					path: '',
					component: () => import('@/modules/managers/galleryManagement/GalleryManagementHint.vue'),
				},
				{
					path: ':id',
					component: () => import('@/modules/managers/galleryManagement/GalleryManagementDetail.vue'),
					props: true
				}
			]
		}
	];

export default routes;
