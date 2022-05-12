import { mount } from '@vue/test-utils'
import axios from 'axios'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import RoleDetailTab from './RoleDetailTab.vue'

const mockedRole = {
    name: 'dev',
    description: 'dev',
    roleTypeCD: 'DEV_ROLE',
    code: '1234',
    roleTypeID: 29,
    organization: 'DEFAULT_TENANT',
    isPublic: true
}

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() => Promise.resolve({ data: [] }))
}

const factory = () => {
    return mount(RoleDetailTab, {
        global: {
            stubs: {
                Card,
                Checkbox,
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
    it('shows filled input fields when role is passed', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedRole: mockedRole })
        const nameInput = wrapper.find('[data-test="name-input"]')
        const codeInput = wrapper.find('[data-test="code-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        expect(wrapper.vm.role).toStrictEqual(mockedRole)

        expect(nameInput.wrapperElement._value).toBe('dev')
        expect(codeInput.wrapperElement._value).toBe('1234')
        expect(descriptionInput.wrapperElement._value).toBe('dev')
    })

    it('emits correct value on input change', async () => {
        const wrapper = factory()
        await flushPromises()

        const nameInput = wrapper.find('[data-test="name-input"]')
        const codeInput = wrapper.find('[data-test="code-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        await nameInput.setValue('test name')
        expect(wrapper.emitted().fieldChanged[0][0].value).toBe('test name')

        await codeInput.setValue('test code')
        expect(wrapper.emitted().fieldChanged[1][0].value).toBe('test code')

        await descriptionInput.setValue('test description')
        expect(wrapper.emitted().fieldChanged[2][0].value).toBe('test description')
    })
})
