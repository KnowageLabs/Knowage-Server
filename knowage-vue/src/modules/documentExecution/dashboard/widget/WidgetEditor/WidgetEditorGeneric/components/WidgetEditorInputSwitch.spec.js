import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import mockedModel from '../mockedModel'
import InputSwitch from 'primevue/inputswitch'
import PrimeVue from 'primevue/config'
import WidgetEditorInputSwitch from './WidgetEditorInputSwitch.vue'

const factory = (widgetModel) => {
    return mount(WidgetEditorInputSwitch, {
        provide: [PrimeVue],
        props: {
            widgetModel: widgetModel,
            property: 'settings.pagination.enabled',
            label: 'dashboard.widgetEditor.pagination',
            settings: {}
        },
        global: {
            directives: {
                tooltip() {}
            },
            provide: {},
            plugins: [createTestingPinia()],
            stubs: {
                InputSwitch
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('WidgetEditorCheckbox', () => {
    it('should show a data filled view if the model has been already set', async () => {
        const wrapper = factory(mockedModel)
        expect(wrapper.html()).toContain('dashboard.widgetEditor.pagination')
        expect(wrapper.vm.checked).toBe(true)
    })
})
