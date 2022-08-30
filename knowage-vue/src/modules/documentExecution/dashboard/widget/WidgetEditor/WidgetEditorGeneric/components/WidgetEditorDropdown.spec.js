import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Dropdown from 'primevue/dropdown'
import mockedModel from '../mockedModel'
import PrimeVue from 'primevue/config'
import WidgetEditorDropdown from './WidgetEditorDropdown.vue'

const factory = (widgetModel) => {
    return mount(WidgetEditorDropdown, {
        provide: [PrimeVue],
        props: {
            widgetModel: widgetModel,
            property: 'temp.selectedColumn.aggregation',
            label: 'qbe.simpleTable.aggregation',
            settings: {}
        },
        global: {
            directives: {
                tooltip() {}
            },
            provide: {},
            plugins: [createTestingPinia()],
            stubs: {
                Dropdown
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('WidgetEditorDropdown', () => {
    it('should show a data filled view if the model has been already set', async () => {
        const wrapper = factory(mockedModel)
        expect(wrapper.html()).toContain('qbe.simpleTable.aggregation')
        expect(wrapper.vm.modelValue).toBe('MAX')
    })
})
