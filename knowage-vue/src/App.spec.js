import App from './App.vue'
import { shallowMount } from '@vue/test-utils'
import { createStore } from 'vuex'

const store = createStore({
  state() {
    return {
      locale: { country: 'IT', language: 'it' }
    }
  }
})

describe('App', () => {
  test('uses mounts', () => {
    const wrapper = shallowMount(App, {
      global: {
        plugins: [store],
        stubs: ['router-link', 'router-view'],
        mocks: {
          $t: (msg) => msg
        }
      }
    })
    expect(typeof wrapper.data).toBe('undefined')
  })
})
