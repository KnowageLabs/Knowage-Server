import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import FunctionalitiesManagementDetail from './FunctionalitiesManagementDetail.vue'
import Toolbar from 'primevue/toolbar'

const mockedFunctionality = {
    id: 1,
    parentId: 2,
    name: 'Demos name',
    code: 'Demos code',
    description: 'Demos description',
    createRoles: [
        { id: 1, name: 'dev' },
        { id: 2, name: 'user' },
        { id: 3, name: 'admin' }
    ],
    devRoles: [
        { id: 1, name: 'dev' },
        { id: 2, name: 'user' },
        { id: 3, name: 'admin' }
    ],
    execRoles: [
        { id: 1, name: 'dev' },
        { id: 2, name: 'user' },
        { id: 3, name: 'admin' }
    ],
    testRoles: [
        { id: 1, name: 'dev' },
        { id: 2, name: 'user' },
        { id: 3, name: 'admin' }
    ]
}

const mockedRoles = [
    { id: 1, name: 'dev' },
    { id: 2, name: 'user' },
    { id: 3, name: 'admin' }
]

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(FunctionalitiesManagementDetail, {
        props: {
            functionality: mockedFunctionality,
            rolesShort: mockedRoles
        },
        global: {
            stubs: { Button, Card, Checkbox, Column, DataTable, InputText, KnValidationMessages, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Functionalities Detail', () => {
    it('shows an empty form if add new button is clicked', async () => {
        const wrapper = factory()

        await wrapper.setProps({ functionality: null })

        const nameInput = wrapper.find('[data-test="name-input"]')
        const codeInput = wrapper.find('[data-test="code-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        expect(wrapper.vm.selectedFolder).toStrictEqual({})
        expect(nameInput.wrapperElement._value).toBeFalsy()
        expect(codeInput.wrapperElement._value).toBeFalsy()
        expect(descriptionInput.wrapperElement._value).toBeFalsy()
    })

    it('shows a detail if one item is seletected from the tree', () => {
        const wrapper = factory()

        const nameInput = wrapper.find('[data-test="name-input"]')
        const codeInput = wrapper.find('[data-test="code-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        expect(wrapper.vm.selectedFolder).toStrictEqual(mockedFunctionality)
        expect(nameInput.wrapperElement._value).toBe('Demos name')
        expect(codeInput.wrapperElement._value).toBe('Demos code')
        expect(descriptionInput.wrapperElement._value).toBe('Demos description')
    })

    it('disables the save button if one required input is empty', async () => {
        const wrapper = factory()

        await wrapper.setProps({ functionality: null })

        expect(wrapper.vm.selectedFolder).toStrictEqual({})
        expect(wrapper.vm.buttonDisabled).toBe(true)
        expect(wrapper.find('[data-test="submit-button"]').element.disabled).toBe(true)
    })
})
