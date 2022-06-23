import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import Card from 'primevue/card'
import Column from 'primevue/column'
import Dropdown from 'primevue/dropdown'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import FunctionsCatalogDatasetEnvironmentTable from './FunctionsCatalogDatasetEnvironmentTable.vue'

const mockedLibraries = [
    {
        name: 'Werkzeug',
        version: '2.0.1'
    },
    {
        name: 'urllib3',
        version: '1.25.11'
    }
]

const factory = () => {
    return mount(FunctionsCatalogDatasetEnvironmentTable, {
        props: {
            libraries: mockedLibraries
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Card,
                Column,
                Dropdown,
                DataTable,
                InputText
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Functions Catalog Dataset Environment Table', () => {
    it('should show the list of available libraries', () => {
        const wrapper = factory()

        expect(wrapper.vm.environmentLibraries).toStrictEqual(mockedLibraries)
        expect(wrapper.html()).toContain('Werkzeug')
        expect(wrapper.html()).toContain('2.0.1')
        expect(wrapper.html()).toContain('urllib3')
        expect(wrapper.html()).toContain('1.25.11')
    })
})
