const routes = [
    {
        path: '/qbe/:id',
        name: 'qbe',
        component: () => import('@/modules/qbe/QBE.vue'),
        props: true
    }
]

export default routes
