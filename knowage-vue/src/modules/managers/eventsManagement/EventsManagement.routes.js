let routes = [
    {
        path: '/events-management',
        name: 'events-management',
        component: () => import('@/modules/managers/eventsManagement/EventsManagement.vue')
    }
]

export default routes
