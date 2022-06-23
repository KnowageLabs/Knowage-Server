import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import TenantManagement from './TenantManagement.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedTenants = [
    {
        MULTITENANT_ID: 1,
        MULTITENANT_NAME: 'DEFAULT_TENANT',
        MULTITENANT_THEME: 'sbi_default'
    },
    {
        MULTITENANT_ID: 2,
        MULTITENANT_NAME: 'OPTION_2',
        MULTITENANT_THEME: 'sbi_default'
    },
    {
        MULTITENANT_ID: 3,
        MULTITENANT_NAME: 'MARE_555',
        MULTITENANT_THEME: 'sbi_default'
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/license`:
                return Promise.resolve({ data: { hosts: [{ hostName: 'host' }], licenses: { host: '' } } })
            default:
                return Promise.resolve({ data: { root: mockedTenants } })
        }
    }),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: vi.fn()
}

const factory = () => {
    return mount(TenantManagement, {
        global: {
            stubs: {
                Button,
                Card,
                InputText,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Tenant management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('shows "no data" label when loaded empty', async () => {
        $http.get = $http.get.mockReturnValueOnce(Promise.resolve({ data: { hosts: [{ hostName: '' }] } }))
        const wrapper = factory()

        await flushPromises()
        expect(wrapper.find('[data-test="tenants-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Tenant Management', () => {
    it('deletes tenant after clicking on delete icon', async () => {
        const wrapper = factory()
        await flushPromises()

        expect(wrapper.vm.multitenants.length).toBe(3)

        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteTenant(1)
        expect($http.delete).toHaveBeenCalledTimes(1)
        expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'multitenant', { data: 1 })
    })
    it('opens empty detail form when the ' + ' button is clicked', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect($router.push).toHaveBeenCalledWith('/tenants-management/new-tenant')
    })
    it('opens filled detail when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/tenants-management/' + 1)
    })
})
