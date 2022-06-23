import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import ConfigurationManagement from './ConfigurationManagement.vue'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import PrimeVue from 'primevue/config'

const mockedConfigurations = [
    {
        valueId: 1,
        label: 'changepwdmodule.number',
        name: 'Number',
        category: 'SECURITY',
        active: false
    },
    {
        valueId: 2,
        label: 'GIS_LAYER_FILE_MAX_SIZE	',
        name: 'GIS LAYER FILE MAX SIZE	',
        category: 'GENERIC_CONFIGURATION',
        active: true
    },
    {
        valueId: 4,
        label: 'SPAGOBI.DATE-FORMAT.format	',
        name: 'DATE FORMAT',
        category: 'DATE-FORMAT',
        active: true
    }
]

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedConfigurations
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}
const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(ConfigurationManagement, {
        attachToDocument: true,
        global: {
            plugins: [PrimeVue],
            stubs: { Button, InputText, ProgressBar, Toolbar },
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

describe('Configuration Management loading', () => {
    it('show progress bar when loading', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('shows error toast if service returns error', async () => {
        // not in this component
    })
    it('shows "no data" label when loaded empty', async () => {
        axios.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()
        await flushPromises()
        expect(wrapper.vm.configurations.length).toBe(0)
        expect(wrapper.find('[data-test="configurations-table"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Configuration Management', () => {
    it('deletes element clicking on delete icon', async () => {
        const wrapper = factory()
        await flushPromises()
        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')
        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteConfiguration(1)
        expect($http.delete).toHaveBeenCalledTimes(1)
        expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/configs/' + 1)
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
        const dataTable = wrapper.find('[data-test="configurations-table"]')
        await dataTable.find('tr td').trigger('click')

        expect(wrapper.vm.formVisible).toBe(true)
        expect(wrapper.vm.selectedConfiguration).toStrictEqual({
            valueId: 1,
            label: 'changepwdmodule.number',
            name: 'Number',
            category: 'SECURITY',
            active: false
        })
    })
})
