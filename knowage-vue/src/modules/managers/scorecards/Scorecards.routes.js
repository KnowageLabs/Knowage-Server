const routes = [
    {
        path: '/scorecards',
        name: 'scorecards',
        component: () => import('@/modules/managers/scorecards/Scorecards.vue')
    }
]

export default routes
