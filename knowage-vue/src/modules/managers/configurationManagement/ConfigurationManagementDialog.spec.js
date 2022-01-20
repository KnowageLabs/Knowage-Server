import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import ConfigurationManagementDialog from './ConfigurationManagementDialog.vue'
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
    return mount(ConfigurationManagementDialog, {
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
        expect(formWrapper.vm.configuration).toStrictEqual({})
        expect(formWrapper.vm.buttonDisabled).toBe(true)
    })
    it('close button returns to list without saving data', () => {})
    it('when save button is clicked data is passed', async () => {
        const mockedConfiguration = {
            label: 'changepwdmodule.number',
            name: 'Number',
            description: 'lalalaladesc',
            category: 'SECURITY',
            active: false
        }
        const formWrapper = factory()
        formWrapper.vm.configuration = mockedConfiguration
        formWrapper.vm.v$.$invalid = false
        formWrapper.vm.handleSubmit()
        await flushPromises()
        expect(axios.post).toHaveBeenCalledTimes(1)
        expect(axios.post).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs', mockedConfiguration)
        expect($store.commit).toHaveBeenCalledTimes(1)

        mockedConfiguration.id = 1
        formWrapper.vm.configuration = mockedConfiguration
        formWrapper.vm.handleSubmit()
        await flushPromises()
        expect(axios.put).toHaveBeenCalledTimes(1)
        expect(axios.put).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/1', mockedConfiguration)
        expect($store.commit).toHaveBeenCalledTimes(2)
    })
})
