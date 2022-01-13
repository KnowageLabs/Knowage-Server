import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import DatasetTableCard from './DatasetTableCard.vue'
import flushPromises from 'flush-promises'
import Toolbar from 'primevue/toolbar'

const mockedDatasetMetadata = [
    {
        name: 'CACHE_TEST',
        signature: '0b78ecae01d62da1c1604f75086478f9332e1491c677d4353f8c27cee3800c79',
        table: 'sbicache6640e736c84511ebbb72b9',
        dimension: 0
    },
    {
        name: 'CACHE_TEST 3',
        signature: '4a30fe7b45062c4b364dc73c9674aed46332a489d786f367d054577923005c3a',
        table: 'sbicache6c401707c84511ebbb72b9',
        dimension: 0
    }
]

jest.mock('axios')

const $http = {
    put: axios.put.mockImplementation(() => Promise.resolve()),
    delete: axios.delete.mockImplementation(() => Promise.resolve())
}

const $store = {
    commit: jest.fn()
}

const $confirm = {
    require: jest.fn()
}

const factory = (datasetMetadataList) => {
    return mount(DatasetTableCard, {
        props: {
            datasetMetadataList
        },
        global: {
            plugins: [],
            stubs: { Button, Card, Column, DataTable, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Cache Management Dataset Table', () => {
    it("shows 'No metadata message' if no metadata is available", () => {
        const wrapper = factory([])

        expect(wrapper.vm.datasets.length).toBe(0)

        expect(wrapper.find('[data-test="dataset-table"]').html()).toContain('managers.cacheManagement.metadataUnavailable')
    })
    it('if no metadata is available the clean all button is disabled', () => {
        const wrapper = factory([])

        expect(wrapper.vm.datasets.length).toBe(0)
        expect(wrapper.vm.cleanAllDisabled).toBe(true)
    })
    it('removes all present metadata and emits event when clean all button is pressed', async () => {
        const wrapper = factory(mockedDatasetMetadata)

        expect(wrapper.vm.datasets.length).toBe(2)

        await wrapper.find('[data-test="clean-all-button"]').trigger('click')
        await flushPromises()

        await wrapper.vm.cleanAll()

        expect(axios.delete).toHaveBeenCalledTimes(1)
        expect(axios.delete).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/cacheee')
        expect($store.commit).toHaveBeenCalledTimes(1)
        expect(wrapper.emitted()).toHaveProperty('deleted')
    })
    it('removes metadata and emits event when delete button is pressed', async () => {
        const wrapper = factory(mockedDatasetMetadata)
        expect(wrapper.vm.datasets.length).toBe(2)

        await wrapper.find('[data-test="delete-button"]').trigger('click')
        await flushPromises()

        await wrapper.vm.deleteDataset(mockedDatasetMetadata[0].signature)

        expect(axios.put).toHaveBeenCalledTimes(1)
        expect(axios.put).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/cacheee/deleteItems', { namesArray: ['0b78ecae01d62da1c1604f75086478f9332e1491c677d4353f8c27cee3800c79'] })
        expect($store.commit).toHaveBeenCalledTimes(1)
        expect(wrapper.emitted()).toHaveProperty('deleted')
    })
})
