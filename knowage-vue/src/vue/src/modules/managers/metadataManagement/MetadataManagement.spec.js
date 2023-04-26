import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import Listbox from 'primevue/listbox'
import MetadataManagement from './MetadataManagement.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedMetadata = [
    {
        objMetaId: 1,
        label: 'label1',
        name: 'metadata1',
        description: 'description1',
        dataTypeCode: 'SHORT_TEXT'
    },
    {
        objMetaId: 2,
        label: 'label2',
        name: 'metadata2',
        description: 'description2',
        dataTypeCode: 'SHORT_TEXT'
    },
    {
        objMetaId: 5,
        label: 'label3',
        name: 'metadata3',
        description: 'description3',
        dataTypeCode: 'FILE'
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedMetadata
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(MetadataManagement, {
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Card,
                InputText,
                Listbox,
                ProgressBar,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $confirm,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Metadata Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('shows "no data" label when loaded empty', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.metadataList.length).toBe(0)
        expect(wrapper.find('[data-test="metadata-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Metadata Management', () => {
    it('deletes metadata after clicking on delete icon', async () => {
        const wrapper = factory()
        await flushPromises()

        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteMetadata(1)
        expect($http.delete).toHaveBeenCalledTimes(1)
        expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/objMetadata/' + 1)
    })

    it('adds empty card with inputs and tabs in detail when the "+" button is clicked', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect(wrapper.vm.formVisible).toBe(true)
        expect(wrapper.find('[data-test="metadata-form"]').exists()).toBe(true)
    })

    it('shows selects correct metadata object when clicked row data', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect(wrapper.vm.formVisible).toBe(true)
        expect(wrapper.vm.selectedMetadata).toStrictEqual({
            id: 1,
            label: 'label1',
            name: 'metadata1',
            description: 'description1',
            dataType: 'SHORT_TEXT'
        })
    })
})

describe('Metadata Management Search', () => {
    it('filters the list if a label or name is provided', async () => {
        const wrapper = factory()
        await flushPromises()
        const metadataList = wrapper.find('[data-test="metadata-list"]')
        const searchInput = metadataList.find('input')

        expect(metadataList.html()).toContain('metadata1')
        expect(metadataList.html()).toContain('metadata2')
        expect(metadataList.html()).toContain('metadata3')

        // Name
        await searchInput.setValue('metadata2')
        await metadataList.trigger('filter')
        expect(metadataList.html()).not.toContain('metadata1')
        expect(metadataList.html()).toContain('metadata2')
        expect(metadataList.html()).not.toContain('metadata3')

        // Data type
        await searchInput.setValue('FILE')
        await metadataList.trigger('filter')
        expect(metadataList.html()).not.toContain('metadata1')
        expect(metadataList.html()).not.toContain('metadata2')
        expect(metadataList.html()).toContain('metadata3')
    })
    it('returns no data if the label is not present', async () => {
        const wrapper = factory()
        await flushPromises()
        const metadataList = wrapper.find('[data-test="metadata-list"]')
        const searchInput = metadataList.find('input')

        expect(metadataList.html()).toContain('metadata1')
        expect(metadataList.html()).toContain('metadata2')
        expect(metadataList.html()).toContain('metadata3')

        await searchInput.setValue('not present value')
        await metadataList.trigger('filter')

        expect(metadataList.html()).not.toContain('metadata1')
        expect(metadataList.html()).not.toContain('metadata2')
        expect(metadataList.html()).not.toContain('metadata3')
    })
})
