import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import mockedModel from './mockedModel'
import Button from 'primevue/button'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import WidgetEditorGeneric from './WidgetEditorGeneric.vue'
import Toolbar from 'primevue/toolbar'
import WidgetEditorInputSwitch from './components/WidgetEditorInputSwitch.vue'
import WidgetEditorInputText from './components/WidgetEditorInputText.vue'
import WidgetEditorDataTable from './components/WidgetEditorDataTable.vue'
import WidgetEditorDropdown from './components/WidgetEditorDropdown.vue'
import WidgetEditorAccordion from './components/WidgetEditorAccordion.vue'
import WidgetEditorCheckbox from './components/WidgetEditorCheckbox.vue'
import dataDescriptor from '../WidgetEditorDataTab/WidgetEditorGenericDescriptor.json'

const factory = (widgetModel, propDescriptor) => {
    return mount(WidgetEditorGeneric, {
        provide: [PrimeVue],
        props: {
            widgetModel: widgetModel,
            propDescriptor: propDescriptor
        },
        global: {
            directives: {
                tooltip() {}
            },
            provide: {},
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Column,
                InputText,
                Toolbar,
                WidgetEditorInputSwitch,
                WidgetEditorInputText,
                WidgetEditorDataTable,
                WidgetEditorDropdown,
                WidgetEditorAccordion,
                WidgetEditorCheckbox,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('WidgetEditorGeneric', () => {
    it('should show a data filled view if the model has been already set', async () => {
        const wrapper = factory(mockedModel, dataDescriptor)
        expect(wrapper.vm.model).toStrictEqual(mockedModel)

        expect(wrapper.find('[data-test="widget-editor-input-text-input"]').wrapperElement._value).toBe(mockedModel.settings.pagination.itemsNumber)
        expect(wrapper.find('[data-test="widget-editor-data-table"]').html()).toContain(mockedModel.columns[0].alias)
        expect(wrapper.find('[data-test="widget-editor-data-table"]').html()).toContain(mockedModel.columns[1].alias)
        expect(wrapper.find('[data-test="widget-editor-data-table"]').html()).toContain(mockedModel.columns[1].aggregation)
    })
})
