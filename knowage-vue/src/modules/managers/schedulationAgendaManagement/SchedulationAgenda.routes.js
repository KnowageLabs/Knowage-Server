let routes = [
    {
        path: '/schedulation-agenda',
        name: 'schedulation-agenda',
        component: () => import('@/modules/managers/schedulationAgendaManagement/SchedulationAgenda.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/schedulationAgendaManagement/SchedulationAgendaHint.vue')
            },
            {
                path: 'search-result',
                name: 'search-result',
                component: () => import('@/modules/managers/schedulationAgendaManagement/SchedulationAgendaDisplay.vue'),
                props: true
            }
        ]
    }
]

export default routes
