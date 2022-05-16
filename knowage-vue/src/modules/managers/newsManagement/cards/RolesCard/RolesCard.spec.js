import { mount } from '@vue/test-utils'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import RolesCard from './RolesCard.vue'
import Toolbar from 'primevue/toolbar'

const mockedRoles = [
    {
        id: 1,
        name: '/kte/admin'
    },
    {
        id: 2,
        name: 'user'
    },
    {
        id: 3,
        name: 'dev'
    }
]
const mockedSelected = [
    {
        id: 1,
        name: '/kte/admin'
    },
    {
        id: 3,
        name: 'dev'
    }
]

const factory = (categoryList, selected) => {
    return mount(RolesCard, {
        props: {
            categoryList,
            selected
        },
        global: {
            stubs: {
                Column,
                Card,
                DataTable,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Roles Card', () => {
    it("shows 'no data' label when loaded empty", () => {
        const wrapper = factory([], [])

        expect(wrapper.props('categoryList').length).toBe(0)

        expect(wrapper.find('[data-test="data-table"]').html()).toContain('common.info.noDataFound')
    })
    it('one or more rows are selected if the detail has selections', async () => {
        const wrapper = factory(mockedRoles, mockedSelected)
        const dataTable = wrapper.find('[data-test="data-table"]')

        expect(dataTable.html()).toContain('/kte/admin')
        expect(dataTable.html()).toContain('dev')

        expect(wrapper.vm.selectedCategories).toStrictEqual(mockedSelected)
    })
    it('one or more rows are selected and changed event is emitted', async () => {
        const wrapper = factory(mockedRoles, [])
        const dataTable = wrapper.find('[data-test="data-table"]')

        expect(dataTable.html()).toContain('/kte/admin')
        expect(dataTable.html()).toContain('user')
        expect(dataTable.html()).toContain('dev')

        await dataTable.find('.p-checkbox-icon').trigger('click')
        wrapper.vm.setDirty()

        expect(wrapper.vm.selectedCategories).toStrictEqual(mockedRoles)
        expect(wrapper.emitted()).toHaveProperty('changed')
        expect(wrapper.emitted().changed[1][0]).toStrictEqual(mockedRoles)
    })
})
