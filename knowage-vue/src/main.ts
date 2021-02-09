import { createApp } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config'
import router from './app.routes.js'
import store from './app.store.js'

import axios from 'axios'
import VueAxios from 'vue-axios'
import interceptor from './axios.js'
interceptor()

import 'primevue/resources/themes/mdc-light-indigo/theme.css'
import 'primevue/resources/primevue.min.css'               
import 'primeicons/primeicons.css'
import '@fortawesome/fontawesome-free/css/all.css'
import 'primeflex/primeflex.css'

import ToastService from 'primevue/toastservice'
import Button from 'primevue/button'
import Card from 'primevue/card'

import Tooltip from 'primevue/tooltip'

import i18n from '@/App.i18n'


createApp(App)
    .use(VueAxios, axios)
    .use(store)
    .use(router)
    .use(i18n)
    .use(PrimeVue)
    .use(ToastService)

    .provide('$axios', axios)
// eslint-disable-next-line
// @ts-ignore
    .directive('tooltip', Tooltip)

    .component('Button', Button)
    .component('Card', Card)

    .mount('#app')

