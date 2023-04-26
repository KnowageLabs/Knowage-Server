const routes = [
    {
        path: '/kpi-edit/new-kpi',
        name: 'kpi-edit-new-kpi',
        component: () => import('@/modules/kpi/kpiDocumentDesigner/KpiDocumentDesigner.vue')
    },
    {
        path: '/kpi-edit/:id',
        name: 'kpi-edit-edit-kpi',
        props: true,
        component: () => import('@/modules/kpi/kpiDocumentDesigner/KpiDocumentDesigner.vue')
    }
]

export default routes
