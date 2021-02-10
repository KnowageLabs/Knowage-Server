import { shallowMount } from '@vue/test-utils'
import Knmenu from '../Knmenu.vue'

describe('Kmenu', () => {
    const wrapper = shallowMount(Knmenu, {
        propsData: {
          items: []
        }
    })

    it('has test value set to test', () => {
        expect(wrapper.vm.showProfileMenu).toBe(false)
      })
    it('returns empty array if no props is set', () => {
    expect(wrapper.vm.fixedMenu[0].label).toBe('home')
    })
  })