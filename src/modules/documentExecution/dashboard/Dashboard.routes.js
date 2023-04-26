const routes = [
    {
        path: '/document-execution/dashboard/:id',
        name: 'dashboard',
        component: () => import('@/modules/documentExecution/dashboard/DashboardController.vue'),
        props: true
    }
]

export default routes
