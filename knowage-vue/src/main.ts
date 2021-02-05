import { createApp } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config'
import router from './app.routes.js'
import store from './app.store.js'
import axios from 'axios'


import 'primevue/resources/themes/mdc-light-indigo/theme.css'
import 'primevue/resources/primevue.min.css'               
import 'primeicons/primeicons.css'
import '@fortawesome/fontawesome-free/css/all.css'
import 'primeflex/primeflex.css'


import Button from 'primevue/button'
import Card from 'primevue/card'

axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*'
axios.defaults.headers.common['Content-Type'] ='application/json;charset=utf-8'
axios.defaults.headers.common['Crossorigin'] ='true'


createApp(App)
    .use(store)
    .use(router)
    .use(PrimeVue)

    .component('Button', Button)
    .component('Card', Card)

    .mount('#app')

