import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import PrimeVue from 'primevue/config'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import WidgetEditorTabs from './WidgetEditorTabs.vue'

const factory = () => {
    return mount(WidgetEditorTabs, {
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                TabView,
                TabPanel,
                WidgetEditorDataTab: true,
                WidgetEditorSettingsTab: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('WidgetEditorTabs', () => {
    it('should start on data tab', async () => {
        const wrapper = factory()

        expect(wrapper.vm.activeIndex).toBe(0)
        expect(wrapper.find('[data-test="data-tab"]').exists()).toBe(true)
    })
})
