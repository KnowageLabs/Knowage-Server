import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import PrimeVue from 'primevue/config'
import DatasetEditorDataList from './DatasetEditorDataList.vue'
import mocks from '../../DatasetEditorTestMocks.json'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import Button from 'primevue/button'

const factory = () => {
    return mount(DatasetEditorDataList, {
        props: {
            dashboardDatasetsProp: [],
            availableDatasetsProp: mocks.availableDatasetsMock,
            selectedDatasetsProp: mocks.availableDatasetsMock
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
                Listbox,
                DataDialog: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('DatasetEditorDataList', () => {
    it('should show a list of available datasets when one or more datasets are set in the model', async () => {
        const wrapper = factory()
        expect(wrapper.find('[data-test="dataset-list-item"]').exists()).toBe(true)
    })
    it('should show a prompt if the remove button is clicked on a list item', async () => {
        const wrapper = factory()

        expect(wrapper.find('[data-test="delete-dataset-list-item"]').exists()).toBe(true)
        await wrapper.find('[data-test="delete-dataset-list-item"]').trigger('click')

        expect(wrapper.emitted()['deleteDataset'][0][0]).toStrictEqual(mocks.availableDatasetsMock[0])
    })
    it('should open a dialog when add dataset is clicked', async () => {
        const wrapper = factory()

        expect(wrapper.find('[data-test="add-dataset-button"]').exists()).toBe(true)
        await wrapper.find('[data-test="add-dataset-button"]').trigger('click')

        await nextTick()

        expect(wrapper.find('[data-test="dataset-data-dialog"]').exists()).toBe(true)
    })
})
