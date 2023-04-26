const routes = [
    {
        path: '/glossary-usage',
        name: 'glossary-usage',
        component: () => import('@/modules/managers/glossaryUsage/GlossaryUsage.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/glossaryUsage/GlossaryUsageHint.vue')
            }
        ]
    }
]

export default routes
