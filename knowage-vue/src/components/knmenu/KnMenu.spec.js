import { shallowMount } from '@vue/test-utils'
import Knmenu from './Knmenu.vue'
import { createStore } from 'vuex'
import flushPromises from 'flush-promises'
import Tooltip from 'primevue/tooltip'
import axios from 'axios'

const store = createStore({
  state() {
    return {
      locale: { country: 'IT', language: 'it' }
    }
  }
})

const wrapper = shallowMount(Knmenu, {
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

describe('Kmenu', () => {
  test('is loaded empty', () => {
    expect(wrapper.vm.dynamicUserFunctionalities).toBe(undefined)
    expect(wrapper.vm.commonUserFunctionalities).toBe(undefined)
  })
})

jest.mock('axios', () => ({
  get: jest.fn(() => Promise.resolve({ data: { technicalUserFunctionalities: [{ items: [{ label: 'Data source' }] }] } }))
}))

describe('Kmenu', () => {
  test('is administrator', async () => {
    expect(axios.get).toHaveBeenCalledWith('/knowage/restful-services/3.0/menu/enduser?curr_country=IT&curr_language=it')
    await flushPromises()
    expect(wrapper.vm.technicalUserFunctionalities.length).not.toBe(0)
  })
})
