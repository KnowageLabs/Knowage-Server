import GisRoutes from '@/modules/workspace/gisDocumentDesigner/GisDocumentDesigner.routes.js'

let baseRoutes = [
    {
        path: '/workspace',
        name: 'workspace',
        component: () => import('@/modules/workspace/Workspace.vue'),
        children: [
            {
                path: '',
                component: () => import('@/modules/workspace/WorkspaceHint.vue')
            },
            {
                path: 'recent',
                component: () => import('@/modules/workspace/views/recentView/WorkspaceRecentView.vue')
            },
            {
                path: 'repository/:id',
                component: () => import('@/modules/workspace/views/repositoryView/WorkspaceRepositoryView.vue'),
                props: true
            },
            {
                path: 'data',
                component: () => import('@/modules/workspace/views/dataView/WorkspaceDataView.vue')
            },
            {
                path: 'models',
                component: () => import('@/modules/workspace/views/modelsView/WorkspaceModelsView.vue')
            },
            {
                path: 'models/federation-definition/new-federation',
                name: 'new-federation',
                component: () => import('@/modules/workspace/federationDefinition/WorkspaceFederationDefinition.vue')
            },
            {
                path: 'models/federation-definition/:id',
                name: 'edit-federation',
                component: () => import('@/modules/workspace/federationDefinition/WorkspaceFederationDefinition.vue'),
                props: true
            },
            {
                path: 'analysis',
                component: () => import('@/modules/workspace/views/analysisView/WorkspaceAnalysisView.vue')
            },
            {
                path: 'schedulation',
                component: () => import('@/modules/workspace/views/schedulationView/WorkspaceSchedulationView.vue')
            },
            {
                path: 'advanced',
                component: () => import('@/modules/workspace/views/advancedData/WorkspaceAdvancedDataView.vue')
            }
        ]
    }
]

const routes = baseRoutes.concat(GisRoutes)

export default routes
