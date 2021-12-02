let routes = [
    {
        path: '/data-preparation/:id',
        name: 'data-preparation',
        component: () => import('@/modules/workspace/dataPreparation/DataPreparationDetail.vue'),
        props: true
    }
]

export default routes
