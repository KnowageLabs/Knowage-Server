import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import mockedModel from '../mockedModel'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import WidgetEditorInputText from './WidgetEditorInputText.vue'

const factory = (widgetModel) => {
    return mount(WidgetEditorInputText, {
        provide: [PrimeVue],
        props: {
            widgetModel: widgetModel,
            property: 'settings.pagination.itemsNumber',
            label: 'dashboard.widgetEditor.itemsPerPage',
            settings: {
                type: 'number'
            }
        },
        global: {
            directives: {
                tooltip() {}
            },
            provide: {},
            plugins: [createTestingPinia()],
            stubs: {
                InputText
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('WidgetEditorInputText', () => {
    it('should show a data filled view if the model has been already set', async () => {
        const wrapper = factory(mockedModel)
        expect(wrapper.html()).toContain('dashboard.widgetEditor.itemsPerPage')
        expect(wrapper.vm.modelValue).toBe(23)
    })
})
