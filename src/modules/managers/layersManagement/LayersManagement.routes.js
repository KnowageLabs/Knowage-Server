const routes = [
    {
        path: '/layers-management',
        name: 'layers-management',
        component: () => import('@/modules/managers/layersManagement/LayersManagement.vue')
    }
]

export default routes
