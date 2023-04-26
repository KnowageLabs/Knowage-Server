import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import DatasetEditorDataDetail from './DatasetEditorDataDetail.vue'
import mocks from '../../DatasetEditorTestMocks.json'
import { nextTick } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'
import Card from 'primevue/card'

const factory = () => {
    return mount(DatasetEditorDataDetail, {
        props: {
            selectedDatasetProp: {},
            dashboardDatasetsProp: [],
            documentDriversProp: []
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                KnHint,
                Card,
                InfoCard: true,
                ParamsCard: true,
                IndexesCard: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('DatasetEditorDataDetail', () => {
    it('should start empty suggesting the user to click on add Dataset', async () => {
        const wrapper = factory()
        expect(wrapper.find('[data-test="dataset-hint"]').exists()).toBe(true)
        await wrapper.setProps({ selectedDatasetProp: mocks.availableDatasetsMock[0] })
        await nextTick()
        expect(wrapper.find('[data-test="dataset-hint"]').exists()).toBe(false)
    })
})
