import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import DatasetEditorDataTab from './DatasetEditorDataTab.vue'
import mocks from '../DatasetEditorTestMocks.json'
import { nextTick } from 'vue'

const factory = () => {
    return mount(DatasetEditorDataTab, {
        props: {
            dashboardDatasetsProp: [],
            availableDatasetsProp: mocks.availableDatasetsMock,
            selectedDatasetsProp: [],
            documentDriversProp: []
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                DataList: true,
                DataDetail: true,
                DatasetEditorPreview: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('DatasetEditorDataTab', () => {
    it('should start without any visible preview', async () => {
        const wrapper = factory()
        expect(wrapper.find('[data-test="dataset-preview"]').exists()).toBe(false)
    })
    it('should show a dataset detail when clicking on a dataset from the list', async () => {
        const wrapper = factory()
        wrapper.vm.selectDataset(116)
        await nextTick()
        expect(wrapper.find('[data-test="dataset-detail"]').exists()).toBe(true)
    })
    it('should show a preview on the right when clicking on a dataset from the list', async () => {
        const wrapper = factory()
        wrapper.vm.selectDataset(116)
        await nextTick()
        expect(wrapper.find('[data-test="dataset-preview"]').exists()).toBe(true)
    })
})
