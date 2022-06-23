import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
// import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import TenantDetail from './TenantDetail.vue'

const mockedTenant = {
    MULTITENANT_ID: 1,
    MULTITENANT_NAME: 'DEFAULT_TENANT',
    MULTITENANT_THEME: 'spagobi_bi'
}

const mockedTenantEnabled = {
    MULTITENANT_NAME: 'ENABLE INPUT FIELD',
    MULTITENANT_THEME: 'spagobi_bi'
}

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() => Promise.resolve({ data: [] }))
}

axios.get.mockImplementation(() => Promise.resolve({ data: [] }))

const factory = () => {
    return mount(TenantDetail, {
        props: { listOfThemes: [] },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Card,
                Dropdown,
                KnValidationMessages,
                InputText
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

describe('Role Detail Tab', () => {
    it('opens filled detail when a row is clicked', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedTenant: mockedTenant })
        const nameInput = wrapper.find('[data-test="name-input"]')

        expect(wrapper.vm.tenant).toStrictEqual({ ...mockedTenant, MULTITENANT_IMAGE: [] })

        expect(nameInput.wrapperElement._value).toBe('DEFAULT_TENANT')
    })
    it('shows disabled name input when a row is clicked', async () => {
        const wrapper = factory()

        await wrapper.setProps({ selectedTenant: mockedTenantEnabled })
        expect(wrapper.vm.disableField).toBe(false)

        await wrapper.setProps({ selectedTenant: mockedTenant })
        expect(wrapper.vm.disableField).toBe(true)
    })
})
