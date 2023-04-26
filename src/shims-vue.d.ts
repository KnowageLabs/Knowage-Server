/* eslint-disable */
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'vue/types/vue' {
  import VueRouter, { Route } from 'vue-router'
  interface Vue {
    $router: VueRouter
  }
}

declare module 'vue/i18n/vue' {
  import { I18n } from 'vue-i18n'
  interface Vue {
    $i18n: VueRouter
  }
}
