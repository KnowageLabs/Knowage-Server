import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import mockedModel from '../WidgetEditorGeneric/mockedModel'
import Button from 'primevue/button'
import Card from 'primevue/card'
import PrimeVue from 'primevue/config'
import WidgetEditorDataTab from './WidgetEditorDataTab.vue'
import WidgetEditorHint from '../WidgetEditorHint.vue'

const factory = (propWidget) => {
    return mount(WidgetEditorDataTab, {
        props: {
            propWidget: propWidget
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Card,
                WidgetEditorHint,
                WidgetEditorDataList: true,
                WidgetEditorGeneric: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('WidgetEditorDataTab', () => {
    it('should show an empty view with an hint if the widget is new', async () => {
        const wrapper = factory({ ...mockedModel, columns: [] })

        expect(wrapper.vm.selectedDataset).toBe(null)
        expect(wrapper.vm.propWidget.columns.length).toBe(0)
        expect(wrapper.html()).toContain('dashboard.widgetEditor.hint')
    })
})
