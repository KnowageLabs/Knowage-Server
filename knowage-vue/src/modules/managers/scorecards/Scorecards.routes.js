const routes = [
    {
        path: '/scorecards',
        name: 'scorecards',
        component: () => import('@/modules/managers/scorecards/Scorecards.vue')
    },
    {
        path: '/scorecards/new-scorecard',
        name: 'new-scorecard',
        component: () => import('@/modules/managers/scorecards/ScorecardsDesigner.vue')
    },
    {
        path: '/scorecards/:id',
        name: 'edit-scorecard',
        component: () => import('@/modules/managers/scorecards/ScorecardsDesigner.vue'),
        props: true
    }
]

export default routes
