import { createApp } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config'
import router from './App.routes.js'
import store from './App.store.js'

import axios from 'axios'
import VueAxios from 'vue-axios'
import interceptor from './axios.js'
interceptor()

import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/monokai.css'
import 'codemirror/mode/htmlmixed/htmlmixed.js'
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/mode/python/python.js'
import 'codemirror/mode/xml/xml.js'

import 'primevue/resources/themes/mdc-light-indigo/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'
import '@fortawesome/fontawesome-free/css/all.css'
import 'primeflex/primeflex.css'

import ToastService from 'primevue/toastservice'
import Button from 'primevue/button'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Toolbar from 'primevue/toolbar'

import Tooltip from 'primevue/tooltip'

import ConfirmationService from 'primevue/confirmationservice'

import i18n from '@/App.i18n'

createApp(App)
  .use(VueAxios, axios)
  .use(store)
  .use(router)
  .use(i18n)
  .use(PrimeVue)
  .use(ToastService)
  .use(ConfirmationService)

  // eslint-disable-next-line
  // @ts-ignore
  .directive('tooltip', Tooltip)

  .component('Button', Button)
  .component('Card', Card)
  .component('InputText', InputText)
  .component('Toolbar', Toolbar)

  .mount('#app')
