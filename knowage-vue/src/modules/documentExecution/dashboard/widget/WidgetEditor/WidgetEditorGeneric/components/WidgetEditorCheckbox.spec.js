import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import mockedModel from '../mockedModel'
import Checkbox from 'primevue/checkbox'
import PrimeVue from 'primevue/config'
import WidgetEditorCheckbox from './WidgetEditorCheckbox.vue'

const factory = (widgetModel) => {
    return mount(WidgetEditorCheckbox, {
        provide: [PrimeVue],
        props: {
            widgetModel: widgetModel,
            property: 'temp.selectedColumn.style.enableCustomHeaderTooltip',
            label: 'dashboard.widgetEditor.customHeaderTooltip',
            settings: { true: true }
        },
        global: {
            directives: {
                tooltip() {}
            },
            provide: {},
            plugins: [createTestingPinia()],
            stubs: {
                Checkbox
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
        expect(wrapper.html()).toContain('dashboard.widgetEditor.customHeaderTooltip')
        expect(wrapper.vm.modelValue).toBe(true)
    })
})
