import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import flushPromises from 'flush-promises'
import FunctionalitiesManagementDetail from './FunctionalitiesManagementDetail.vue'
import Toolbar from 'primevue/toolbar'

const mockedFunctionality = {
    id: 2,
    parentId: 1,
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
        { id: 3, name: 'admin' }
    ],
    execRoles: [
        { id: 1, name: 'dev' },
        { id: 2, name: 'user' },
        { id: 3, name: 'admin' }
    ],
    testRoles: [
        { id: 1, name: 'dev' },
        { id: 3, name: 'admin' }
    ]
}

const mockedParentFunctionality = {
    id: 1,
    parentId: null,
    name: 'Functionalities',
    code: 'Functionalities',
    description: 'Functionalities',
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

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedParentFunctionality
        })
    ),
    put: axios.put.mockImplementation(() =>
        Promise.resolve({
            data: mockedFunctionality
        })
    )
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(FunctionalitiesManagementDetail, {
        props: {
            functionality: mockedFunctionality,
            rolesShort: mockedRoles,
            parentId: 1
        },
        global: {
            stubs: { Button, Card, Checkbox, Column, DataTable, InputText, KnValidationMessages, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $store,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
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

    it('shows a selectable list of available roles', async () => {
        const wrapper = factory()

        await flushPromises()

        const rolesTable = wrapper.find('[data-test="roles-table"]')

        expect(rolesTable.html()).toContain('dev')
        expect(rolesTable.html()).toContain('user')
        expect(rolesTable.html()).toContain('admin')
    })

    it('shows an empty detail if top level functionality is clicked', async () => {
        const wrapper = factory()

        await wrapper.setProps({
            functionality: {
                id: 1,
                parentId: null,
                name: 'Demos name',
                code: 'Demos code',
                description: 'Demos description'
            }
        })

        const nameInput = wrapper.find('[data-test="name-input"]')
        const codeInput = wrapper.find('[data-test="code-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        expect(wrapper.vm.selectedFolder.parentId).toBe(null)
        expect(nameInput.exists()).toBe(false)
        expect(codeInput.exists()).toBe(false)
        expect(descriptionInput.exists()).toBe(false)
    })
    it("select all the row if the 'select all' button is clicked", async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.roles[1]).toStrictEqual({
            createRoles: {
                checkable: true
            },
            devRoles: {
                checkable: true
            },
            testRoles: {
                checkable: true
            },
            execRoles: {
                checkable: true
            },
            creation: true,
            development: false,
            execution: true,
            isButtonDisabled: false,
            id: 2,
            name: 'user',
            test: false
        })

        await wrapper.find('[data-test="check-all-2"]').trigger('click')

        expect(wrapper.vm.roles[1]).toStrictEqual({
            createRoles: {
                checkable: true
            },
            devRoles: {
                checkable: true
            },
            testRoles: {
                checkable: true
            },
            execRoles: {
                checkable: true
            },
            creation: true,
            development: true,
            execution: true,
            isButtonDisabled: false,
            id: 2,
            name: 'user',
            test: true
        })
    })
    it("deselect all the row if the 'deselect all' button is clicked", async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.roles[0]).toStrictEqual({
            createRoles: {
                checkable: true
            },
            devRoles: {
                checkable: true
            },
            testRoles: {
                checkable: true
            },
            execRoles: {
                checkable: true
            },
            creation: true,
            development: true,
            execution: true,
            isButtonDisabled: false,
            id: 1,
            name: 'dev',
            test: true
        })

        await wrapper.find('[data-test="uncheck-all-1"]').trigger('click')

        expect(wrapper.vm.roles[0]).toStrictEqual({
            createRoles: {
                checkable: true
            },
            devRoles: {
                checkable: true
            },
            testRoles: {
                checkable: true
            },
            execRoles: {
                checkable: true
            },
            creation: false,
            development: false,
            execution: false,
            isButtonDisabled: false,
            id: 1,
            name: 'dev',
            test: false
        })
    })
    it('saves functionality when save button is clicked', async () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedFolder).toStrictEqual(mockedFunctionality)

        await flushPromises()
        await wrapper.find('[data-test="submit-button"]').trigger('click')

        expect(axios.put).toHaveBeenCalledTimes(1)
        expect(wrapper.emitted()).toHaveProperty('inserted')
    })
})
