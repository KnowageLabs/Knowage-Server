import { mount } from '@vue/test-utils'
import {  describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import PrimeVue from 'primevue/config'
import WidgetButtonBar from './WidgetButtonBar.vue'

const factory = () => {
    return mount(WidgetButtonBar, {
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('WidgetButtonBar', () => {
    it('should emitt editWidget event clicking on edit widget button', async () => {
        const wrapper = factory()
        await wrapper.find('[data-test="edit-widget-button"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('editWidget')
    })
})
