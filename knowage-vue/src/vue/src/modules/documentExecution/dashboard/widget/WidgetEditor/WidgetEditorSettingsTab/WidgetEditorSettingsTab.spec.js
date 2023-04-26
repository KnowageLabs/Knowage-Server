import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import mockedModel from '../WidgetEditorGeneric/mockedModel'
import Button from 'primevue/button'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import WidgetEditorList from '../WidgetEditorGeneric/components/WidgetEditorList.vue'
import WidgetEditorSettingsTab from './WidgetEditorSettingsTab.vue'
import WidgetEditorGeneric from '../WidgetEditorGeneric/WidgetEditorGeneric.vue'

const factory = (propWidget) => {
    return mount(WidgetEditorSettingsTab, {
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
                InputText,
                WidgetEditorList,
                WidgetEditorGeneric
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('WidgetEditorSettingsTabs', () => {
    it('should show a list of setting groups if the setting tab is clicked', async () => {
        const wrapper = factory(mockedModel)

        expect(wrapper.find('[data-test="widget-editor-settings-list"]').html()).toContain('common.style')
        expect(wrapper.find('[data-test="widget-editor-settings-list"]').html()).toContain('dashboard.widgetEditor.conditionalStyle.title')
    })

    it('should show the style cards if the style element is clicked from the list', async () => {
        const wrapper = factory(mockedModel)

        await wrapper.find('[data-test="widget-editor-list-item"]').trigger('click')

        expect(wrapper.vm.selectedSetting).toBe('Style')
        expect(wrapper.find('[data-test="widget-editor-generic"]').html()).toContain('dashboard.widgetEditor.header.title')
        expect(wrapper.find('[data-test="widget-editor-generic"]').html()).toContain('dashboard.widgetEditor.header.enableHeader')
        expect(wrapper.find('[data-test="widget-editor-generic"]').html()).toContain('dashboard.widgetEditor.header.allowMultiline')
        expect(wrapper.find('[data-test="widget-editor-generic"]').html()).toContain('dashboard.widgetEditor.header.allowMultiline')
        expect(wrapper.find('[data-test="widget-editor-generic"]').html()).toContain('dashboard.widgetEditor.header.headerHeight')
        expect(wrapper.find('[data-test="widget-editor-style-toolbar"]').exists()).toBe(true)
    })
})
