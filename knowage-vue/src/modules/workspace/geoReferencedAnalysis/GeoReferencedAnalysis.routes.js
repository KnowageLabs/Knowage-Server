let routes = [
    {
        path: '/geo-ref',
        name: 'geo-ref',
        component: () => import('@/modules/workspace/geoReferencedAnalysis/GeoReferencedAnalysis.vue'),
        children: [
            {
                path: 'new',
                name: 'new-geo-ref',
                component: () => import('@/modules/managers/dataSourceManagement/DataSourceTabView/DataSourceDetail.vue')
            },
            {
                path: ':id',
                name: 'edit-datasource',
                component: () => import('@/modules/managers/dataSourceManagement/DataSourceTabView/DataSourceDetail.vue'),
                props: true
            },
            {
                path: 'dnl',
                name: 'dnl-step',
                component: () => import('@/modules/workspace/geoReferencedAnalysis/wizard/GeoReferencedAnalysisStepOne.vue'),
                props: true
            },
            {
                path: 'dsj',
                name: 'dsj-step',
                component: () => import('@/modules/workspace/geoReferencedAnalysis/wizard/GeoReferencedAnalysisStepTwo.vue'),
                props: true
            },
            {
                path: 'ind',
                name: 'ind-step',
                component: () => import('@/modules/workspace/geoReferencedAnalysis/wizard/GeoReferencedAnalysisStepThree.vue'),
                props: true
            },
            {
                path: 'fnm',
                name: 'fnm-step',
                component: () => import('@/modules/workspace/geoReferencedAnalysis/wizard/GeoReferencedAnalysisStepFour.vue'),
                props: true
            }
        ]
    }
]

export default routes
