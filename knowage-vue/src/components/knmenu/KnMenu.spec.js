import { shallowMount } from '@vue/test-utils'
import Knmenu from './Knmenu.vue'
import { createStore } from 'vuex'
import Tooltip from 'primevue/tooltip'

const store = createStore({
  state() {
    return {
      locale: { country: 'IT', language: 'it' }
    }
  }
})

describe('Kmenu', () => {
  test('is loaded empty', () => {
    const wrapper = shallowMount(Knmenu, {
      propsData: {
        items: []
      },
      global: {
        directives: {
          tooltip: Tooltip
        },
        plugins: [store],
        mocks: {
          $t: (msg) => msg
        }
      }
    })
    expect(wrapper.vm.dynamicUserFunctionalities[0]).toBe(undefined)
    expect(wrapper.vm.commonUserFunctionalities[0]).toBe(undefined)
  })
})
