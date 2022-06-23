import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import FunctionsCatalogDatatable from './FunctionsCatalogDatatable.vue'
import Toolbar from 'primevue/toolbar'

const mockedFunctions = [
    {
        id: '4e230432-2332-4efa-97cc-66a42a29523b',
        name: 'echo_function',
        owner: 'demo_admin',
        label: 'echo_function',
        type: 'Machine Learning'
    },
    {
        id: '5196e42e-ae71-44e6-a1ac-854fba2144cf',
        name: 'Logarithm',
        owner: 'demo_admin',
        label: 'Logarithm',
        type: 'Utilities'
    },
    {
        id: '599b009b-707e-4033-ad1c-02f6bec275e7',
        name: 'Toy function',
        owner: 'demo_admin',
        label: 'toy_dataset_function',
        type: 'Machine Learning'
    }
]

const mockedUser = {
    userId: 'demo_admin',
    isSuperadmin: true,
    functionalities: ['FunctionsCatalogManagement']
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(FunctionsCatalogDatatable, {
        props: {
            items: mockedFunctions,
            user: mockedUser
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Button,
                Column,
                DataTable,
                InputText,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $confirm
            }
        }
    })
}

describe('Function Catalog Datatable', () => {
    it('shows progress bar when loading', async () => {
        const wrapper = factory()

        await wrapper.setProps({ items: [], propLoading: true })

        expect(wrapper.vm.functions.length).toBe(0)
        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.html()).toContain('common.info.dataLoading')
    })
    it('should shows an hint when loaded empty', async () => {
        const wrapper = factory()

        await wrapper.setProps({ items: [] })

        expect(wrapper.vm.functions.length).toBe(0)
        expect(wrapper.vm.loading).toBe(false)
        expect(wrapper.html()).toContain('managers.functionsCatalog.noFunctionsFound')
    })
    it('shows loaded functions', () => {
        const wrapper = factory()

        expect(wrapper.vm.functions).toStrictEqual(mockedFunctions)
        expect(wrapper.html()).toContain('echo_function')
        expect(wrapper.html()).toContain('Logarithm')
        expect(wrapper.html()).toContain('Toy function')
        expect(wrapper.html()).toContain('toy_dataset_function')
        expect(wrapper.html()).toContain('demo_admin')
        expect(wrapper.html()).toContain('Machine Learning')
    })
    it('shows a prompt when user click on a function delete button to delete it', async () => {
        const wrapper = factory()

        expect(wrapper.vm.functions).toStrictEqual(mockedFunctions)

        await wrapper.find('[data-test="delete-button-4e230432-2332-4efa-97cc-66a42a29523b"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)
    })
})
