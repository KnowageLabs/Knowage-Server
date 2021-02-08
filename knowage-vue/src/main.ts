import { createApp } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config'
import router from './App.routes.js'
import store from './App.store.js'

import interceptor from './axios';
interceptor()

import 'primevue/resources/themes/mdc-light-indigo/theme.css'
import 'primevue/resources/primevue.min.css'               
import 'primeicons/primeicons.css'
import '@fortawesome/fontawesome-free/css/all.css'
import 'primeflex/primeflex.css'

import ToastService from 'primevue/toastservice';
import Button from 'primevue/button'
import Card from 'primevue/card'

import i18n from '@/App.i18n'


createApp(App)
    .use(store)
    .use(router)
    .use(i18n)
    .use(PrimeVue)
    .use(ToastService)

    .component('Button', Button)
    .component('Card', Card)

    .mount('#app')

