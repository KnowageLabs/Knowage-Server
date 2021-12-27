import { createApp } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config'
import router from './App.routes.js'
import store from './App.store.js'

import VueAxios from 'vue-axios'
import interceptor from './axios.js'

import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/monokai.css'
import 'codemirror/theme/eclipse.css'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/hint/sql-hint.js'
import 'codemirror/addon/lint/lint.js'
import 'codemirror/addon/selection/mark-selection.js'
import 'codemirror/mode/htmlmixed/htmlmixed.js'
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/mode/python/python.js'
import 'codemirror/mode/xml/xml.js'
import 'codemirror/mode/sql/sql.js'
import 'codemirror/mode/groovy/groovy.js'
import 'codemirror/mode/clike/clike.js'
import 'codemirror/mode/mathematica/mathematica.js'

import 'primevue/resources/themes/mdc-light-indigo/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'
import '@fortawesome/fontawesome-free/css/all.css'
import 'primeflex/primeflex.css'
import '@/assets/css/dialects-icons.css'

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


createApp(App)
    .use(VueAxios, interceptor)
    .use(store)
    .use(router)
    .use(i18n)
    .use(PrimeVue)
    .use(ToastService)
    .use(ConfirmationService)
    .use(internationalizationPlugin, store.state.internationalization)

    .directive('badge', BadgeDirective)
    .directive('tooltip', Tooltip)

    .component('Button', Button)
    .component('Card', Card)
    .component('InputText', InputText)
    .component('ProgressBar', ProgressBar)
    .component('Toolbar', Toolbar)

    .mount('#app')
