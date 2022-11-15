import { createRouter, createWebHistory } from 'vue-router'
import IframeRenderer from '@/modules/commons/IframeRenderer.vue'
import managersRoutes from '@/modules/managers/managers.routes.js'
import importExportRoutes from '@/modules/importExport/ImportExport.routes.js'
import kpiRoutes from '@/modules/kpi/kpi.routes.js'
import documentExecutionRoutes from '@/modules/documentExecution/documentExecution.routes.js'
import documentBrowserRoutes from '@/modules/documentBrowser/DocumentBrowser.routes.js'
import workspaceRoutes from '@/modules/workspace/workspace.routes.js'
import overlayRoutes from '@/overlay/Overlay.routes.js'
import authHelper from '@/helpers/commons/authHelper'
import dataPreparationRoutes from '@/modules/workspace/dataPreparation/DataPreparation.routes.js'
import { loadLanguageAsync } from '@/App.i18n.js'
import axios from 'axios'
import store from './App.store'
import i18n from '@/App.i18n'

const baseRoutes = [
    {
        path: '/',
        name: 'home',
        component: () => import('@/views/Home.vue')
    },
    {
        path: '/about',
        name: 'about',
        component: () => import('@/views/About.vue')
    },
    {
        path: '/externalUrl/',
        name: 'externalUrl',
        component: IframeRenderer,
        props: (route) => ({ url: route.params.url, externalLink: true })
    },
    {
        path: '/knowage/servlet/:catchAll(.*)',
        name: 'knowageUrl',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/restful-services/publish:catchAll(.*)',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/restful-services/signup:catchAll(.*)',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/restful-services/2.0/installconfig',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/themes:catchAll(.*)',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/login',
        name: 'login',
        redirect: process.env.VUE_APP_HOST_URL + '/knowage/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE'
    },
    {
        path: '/:catchAll(.*)',
        component: () => import('@/modules/commons/404.vue')
    }
]

const routes = baseRoutes
    .concat(managersRoutes)
    .concat(importExportRoutes)
    .concat(kpiRoutes)
    .concat(documentExecutionRoutes)
    .concat(documentBrowserRoutes)
    .concat(workspaceRoutes)
    .concat(overlayRoutes)
    .concat(dataPreparationRoutes)

const router = createRouter({
    base: process.env.VUE_APP_PUBLIC_PATH,
    history: createWebHistory(process.env.VUE_APP_PUBLIC_PATH),
    routes
})

router.beforeEach((to, from, next) => {
    if (localStorage.getItem('locale')) loadLanguageAsync(localStorage.getItem('locale')).then(() => next())
    const checkRequired = !('/' == to.fullPath && '/' == from.fullPath)
    const loggedIn = localStorage.getItem('token')

    let docTypesRegEx = /registry|document-composite|report|office-doc|olap|map|report|kpi|dossier|etl/
    if (checkRequired && !loggedIn) {
        authHelper.handleUnauthorized()
    } else if (to.fullPath.startsWith('/document-browser') && docTypesRegEx.test(to.fullPath)) {
        let params = `label=${to.params.id}`

        let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/documentexecution/correctRolesForExecution?` + params

        axios.get(url).then((response) => {
            let correctRolesForExecution = response.data

            if (correctRolesForExecution.length == 0) {
                store.commit('setError', {
                    title: i18n.global.t('common.error.generic'),
                    msg: i18n.global.t('documentExecution.main.userRoleError')
                })

                return false
            } else {
                next()
            }
        })
    } else {
        next()
    }
})

export default router
