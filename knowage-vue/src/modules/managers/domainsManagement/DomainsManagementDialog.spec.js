import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import DomainsManagementDialog from './DomainsManagementDialog.vue'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'

jest.mock('axios')

const $http = {
    post: axios.post.mockImplementation(() => Promise.resolve()),
    put: axios.put.mockImplementation(() => Promise.resolve())
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(DomainsManagementDialog, {
        global: {
            plugins: [PrimeVue],
            stubs: { Button, InputText },
            mocks: {
                $t: (msg) => msg,
                $store,
                $http
            }
        }
    })
}

describe('Domains Management Dialog', () => {
    it('save button is disabled if a mandatory input is empty', () => {
        const formWrapper = factory()
        expect(formWrapper.vm.domain).toStrictEqual({})
        expect(formWrapper.vm.buttonDisabled).toBe(true)
    })
    it('close button returns to list without saving data', () => {})
    it('when save button is clicked data is passed', async () => {
        const mockedDomain = {
            valueCd: 'QUERY',
            valueName: 'sbidomains.nm.query',
            domainCode: 'INPUT_TYPE',
            domainName: 'input mode and values',
            valueDescription: 'sbidomains.ds.query'
        }

        const formWrapper = factory()
        formWrapper.vm.domain = mockedDomain
        formWrapper.vm.v$.$invalid = false
        formWrapper.vm.handleSubmit()
        await flushPromises()
        expect(axios.post).toHaveBeenCalledTimes(1)
        expect(axios.post).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/domains', mockedDomain)
        // shows success info if data is saved
        expect($store.commit).toHaveBeenCalledTimes(1)

        mockedDomain.valueId = 1
        formWrapper.vm.domain = mockedDomain
        formWrapper.vm.handleSubmit()
        await flushPromises()
        expect(axios.put).toHaveBeenCalledTimes(1)
        expect(axios.put).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/domains/1', mockedDomain)
        expect($store.commit).toHaveBeenCalledTimes(2)
    })
})
