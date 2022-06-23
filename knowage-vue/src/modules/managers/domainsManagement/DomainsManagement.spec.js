import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import DomainsManagement from './DomainsManagement.vue'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedDomains = [
    {
        valueId: 1,
        valueCd: 'QUERY',
        valueName: 'sbidomains.nm.query',
        domainCode: 'INPUT_TYPE',
        domainName: 'input mode and values',
        valueDescription: 'sbidomains.ds.query'
    },
    {
        valueId: 2,
        valueCd: 'SCRIPT',
        valueName: 'sbidomains.nm.script',
        domainCode: 'INPUT_TYPE',
        domainName: 'input mode and values',
        valueDescription: 'sbidomains.ds.script'
    },
    {
        valueId: 8,
        valueCd: 'DATA_MINING',
        valueName: 'sbidomains.nm.data_mining',
        domainCode: 'BIOBJ_TYPE',
        domainName: 'dummy domain name',
        valueDescription: 'sbidomains.ds.data_mining'
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedDomains
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(DomainsManagement, {
        attachToDocument: true,
        global: {
            plugins: [PrimeVue, createTestingPinia()],
            stubs: { Button, InputText, ProgressBar, Toolbar },
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

describe('Domains Management loading', () => {
    it('show progress bar when loading', async () => {
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
        expect(wrapper.vm.domains.length).toBe(0)
        expect(wrapper.find('[data-test="domains-table"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Domains Management', () => {
    it('deletes element clicking on delete icon', async () => {
        const wrapper = factory()
        await flushPromises()
        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')
        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteDomain(1)
        expect($http.delete).toHaveBeenCalledTimes(1)
        expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/domains/' + 1)
    })
    it("opens empty dialog when the '+' button is clicked", async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')
        await openButton.trigger('click')

        expect(wrapper.vm.formVisible).toBe(true)
    })
    it('open dialog when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        const dataTable = wrapper.find('[data-test="domains-table"]')
        await dataTable.find('tr td').trigger('click')

        expect(wrapper.vm.formVisible).toBe(true)
        expect(wrapper.vm.selectedDomain).toStrictEqual({
            valueId: 1,
            valueCd: 'QUERY',
            valueName: 'sbidomains.nm.query',
            domainCode: 'INPUT_TYPE',
            domainName: 'input mode and values',
            valueDescription: 'sbidomains.ds.query'
        })
    })
})

describe('Domains Management Search', () => {
    it('filters the list if a label (or other column) is provided', async () => {
        const wrapper = factory()
        await flushPromises()
        const dataTable = wrapper.find('[data-test="domains-table"]')
        const inputSearch = wrapper.find('[data-test="search-input"]')

        expect(dataTable.html()).toContain('QUERY')
        expect(dataTable.html()).toContain('SCRIPT')
        expect(dataTable.html()).toContain('DATA_MINING')

        // Value code
        await inputSearch.setValue('QUERY')
        expect(dataTable.html()).toContain('QUERY')
        expect(dataTable.html()).not.toContain('SCRIPT')
        expect(dataTable.html()).not.toContain('DATA_MINING')

        // Value name
        await inputSearch.setValue('sbidomains.nm.script')
        expect(dataTable.html()).not.toContain('QUERY')
        expect(dataTable.html()).toContain('SCRIPT')
        expect(dataTable.html()).not.toContain('DATA_MINING')

        // Domain code
        await inputSearch.setValue('INPUT_TYPE')
        expect(dataTable.html()).toContain('QUERY')
        expect(dataTable.html()).toContain('SCRIPT')
        expect(dataTable.html()).not.toContain('DATA_MINING')

        // Domain name
        await inputSearch.setValue('dummy domain name')
        expect(dataTable.html()).not.toContain('QUERY')
        expect(dataTable.html()).not.toContain('SCRIPT')
        expect(dataTable.html()).toContain('DATA_MINING')

        // Value description
        await inputSearch.setValue('sbidomains.ds.script')
        expect(dataTable.html()).not.toContain('QUERY')
        expect(dataTable.html()).toContain('SCRIPT')
        expect(dataTable.html()).not.toContain('DATA_MINING')
    })

    it('returns no data if the label is not present', async () => {
        const wrapper = factory()
        await flushPromises()
        const dataTable = wrapper.find('[data-test="domains-table"]')
        const inputSearch = wrapper.find('[data-test="search-input"]')

        expect(dataTable.html()).toContain('QUERY')
        expect(dataTable.html()).toContain('SCRIPT')
        expect(dataTable.html()).toContain('DATA_MINING')

        await inputSearch.setValue('not present value')
        expect(dataTable.html()).not.toContain('QUERY')
        expect(dataTable.html()).not.toContain('SCRIPT')
        expect(dataTable.html()).not.toContain('DATA_MINING')
        expect(wrapper.find('[data-test="domains-table"]').html()).toContain('common.info.noDataFound')
    })
})
