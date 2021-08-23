let routes = [
    {
        path: '/glossary-definition',
        name: 'glossary-definition',
        component: () => import('@/modules/managers/glossaryDefinition/GlossaryDefinition.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/managers/glossaryDefinition/GlossaryDefinitionHint.vue')
            }
            // {
            //     path: 'new-glossary',
            //     name: 'new-glossary',
            //     component: () => import('@/modules/managers/glossaryDefinition/GlossaryDefinitionTabView.vue')
            // },
            // {
            //     path: ':id',
            //     name: 'edit-glossary',
            //     component: () => import('@/modules/managers/glossaryDefinition/GlossaryDefinitionTabView.vue'),
            //     props: true
            // }
        ]
    }
]

export default routes
