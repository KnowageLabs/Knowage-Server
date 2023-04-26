const routes = [
    {
        path: '/calendar-management',
        name: 'calendar-management',
        component: () => import('@/modules/managers/calendarManagement/CalendarManagement.vue')
    }
]

export default routes
