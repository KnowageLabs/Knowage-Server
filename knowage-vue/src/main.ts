import { createApp } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config'
import router from './App.routes.js'
import store from './App.store.js'
import { createPinia } from 'pinia'
import { GlobalCmComponent } from 'codemirror-editor-vue3'

import VueAxios from 'vue-axios'
import interceptor from './axios.js'

import '/node_modules/codemirror/lib/codemirror.css'
import '/node_modules/codemirror/theme/monokai.css'
import '/node_modules/codemirror/theme/eclipse.css'
import '/node_modules/codemirror/addon/hint/show-hint.css'
import '/node_modules/codemirror/addon/hint/show-hint.js'
import '/node_modules/codemirror/addon/hint/sql-hint.js'
import '/node_modules/codemirror/addon/lint/lint.js'
import '/node_modules/codemirror/addon/selection/mark-selection.js'
import '/node_modules/codemirror/mode/htmlmixed/htmlmixed.js'
import '/node_modules/codemirror/mode/javascript/javascript.js'
import '/node_modules/codemirror/mode/python/python.js'
import '/node_modules/codemirror/mode/xml/xml.js'
import '/node_modules/codemirror/mode/sql/sql.js'
import '/node_modules/codemirror/mode/css/css.js'
import '/node_modules/codemirror/mode/groovy/groovy.js'
import '/node_modules/codemirror/mode/clike/clike.js'
import '/node_modules/codemirror/mode/mathematica/mathematica.js'

import 'primevue/resources/themes/mdc-light-indigo/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'
import '@fortawesome/fontawesome-free/css/all.css'
import 'primeflex/primeflex.css'
import '@/assets/css/dialects-icons.css'
import 'material-icons/iconfont/material-icons.css'

import ToastService from 'primevue/toastservice'
import Button from 'primevue/button'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import Tooltip from 'primevue/tooltip'
import BadgeDirective from 'primevue/badgedirective'

import ConfirmationService from 'primevue/confirmationservice'
import internationalizationPlugin from './plugins/internationalization.js'

import i18n from '@/App.i18n'

import QBEOperator from './modules/qbe/qbeDialogs/qbeAdvancedFilterDialog/QBEOperator.vue'

if (import.meta.env.DEV) document.domain = 'localhost'

import VueGridLayout from 'vue-grid-layout'

import ResizeObserver from '@vue-toys/resize-observer'

const pinia = createPinia()

const app = createApp(App).use(pinia)

const mainStore = store()

app.use(VueAxios, interceptor)
    .use(mainStore)
    .use(router)
    .use(i18n)
    .use(PrimeVue)
    .use(ToastService)
    .use(ConfirmationService)
    .use(internationalizationPlugin, mainStore.$state.internationalization)
    .use(GlobalCmComponent)
    .use(VueGridLayout)
    .use(ResizeObserver)

    .directive('badge', BadgeDirective)
    .directive('tooltip', Tooltip)

    .component('Button', Button)
    .component('Card', Card)
    .component('InputText', InputText)
    .component('ProgressBar', ProgressBar)
    .component('Toolbar', Toolbar)
    .component('QBEOperator', QBEOperator)

    .mount('#app')
