const routes = [
    {
        path: '/glossary-definition',
        name: 'glossary-definition',
        component: () => import('@/modules/managers/glossaryDefinition/GlossaryDefinition.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/glossaryDefinition/GlossaryDefinitionHint.vue')
            }
        ]
    }
]

export default routes
