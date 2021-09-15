let routes = [
    {
        path: '/dossier/:id',
        name: 'dossier',
        component: () => import('@/modules/documentExecution/dossier/Dossier.vue'),
        props: true
    }
]

export default routes
