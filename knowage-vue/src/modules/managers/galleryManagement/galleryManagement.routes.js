let routes = [
		{
			path: '/knowage/gallerymanagement',
			name: 'galleryManagement',
			component: () => import('@/modules/managers/galleryManagement/GalleryManagement.vue')
		}
	];

	export default routes;