import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { describe, expect, it, vi } from 'vitest'
import Button from 'primevue/button'
import DomainsManagementDialog from './DomainsManagementDialog.vue'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import mainStore from '../../../App.store'

vi.mock('axios')

const $http = {
    post: vi.fn().mockImplementation(() => Promise.resolve()),
    put: vi.fn().mockImplementation(() => Promise.resolve())
}

const factory = () => {
    return mount(DomainsManagementDialog, {
        global: {
            plugins: [PrimeVue, createTestingPinia()],
            stubs: { Button, InputText },
            mocks: {
                $t: (msg) => msg,
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
        const formWrapper = factory()
        const store = mainStore()

        const mockedDomain = {
            valueCd: 'QUERY',
            valueName: 'sbidomains.nm.query',
            domainCode: 'INPUT_TYPE',
            domainName: 'input mode and values',
            valueDescription: 'sbidomains.ds.query'
        }

        formWrapper.vm.domain = mockedDomain
        formWrapper.vm.v$.$invalid = false
        formWrapper.vm.handleSubmit()
        await flushPromises()
        expect($http.post).toHaveBeenCalledTimes(1)
        expect($http.post).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/domains', mockedDomain)
        expect(store.setInfo).toHaveBeenCalledTimes(1)

        mockedDomain.valueId = 1
        formWrapper.vm.domain = mockedDomain
        formWrapper.vm.handleSubmit()
        await flushPromises()
        expect($http.put).toHaveBeenCalledTimes(1)
        expect($http.put).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/domains/1', mockedDomain)
        expect(store.setInfo).toHaveBeenCalledTimes(2)
    })
})
