const routes = [
    {
        path: '/metadata-management',
        name: 'metadata-management',
        component: () => import('@/modules/managers/metadataManagement/MetadataManagement.vue')
    }
]

export default routes
