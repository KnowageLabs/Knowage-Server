import { mount } from '@vue/test-utils'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import DomainCategoryTab from './DomainCategoryTab.vue'
import Toolbar from 'primevue/toolbar'

const mockedOptions = [
    { categoryId: 1, categoryName: 'Default Model Category' },
    { categoryId: 2, categoryName: 'Examples' },
    { categoryId: 3, categoryName: 'Big Data' },
    { categoryId: 4, categoryName: 'Sales' }
]

const mockedSelected = [
    { categoryId: 2, categoryName: 'Examples' },
    { categoryId: 4, categoryName: 'Sales' }
]

const factory = (categoryList, selected) => {
    return mount(DomainCategoryTab, {
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

describe('Domain Category Tab', () => {
    it("shows 'no data' label when loaded empty", () => {
        const wrapper = factory([], [])

        expect(wrapper.props('categoryList').length).toBe(0)

        expect(wrapper.find('[data-test="data-table"]').html()).toContain('common.info.noDataFound')
    })
    it('one or more rows are selected if the detail has selections', async () => {
        const wrapper = factory(mockedOptions, mockedSelected)
        const dataTable = wrapper.find('[data-test="data-table"]')

        expect(dataTable.html()).toContain('Examples')
        expect(dataTable.html()).toContain('Sales')

        expect(wrapper.vm.selectedCategories).toStrictEqual(mockedSelected)
    })
    it('one or more rows are selected and changed event is emitted', async () => {
        const wrapper = factory(mockedOptions, [])
        const dataTable = wrapper.find('[data-test="data-table"]')

        expect(dataTable.html()).toContain('Default Model Category')
        expect(dataTable.html()).toContain('Big Data')

        await dataTable.find('.p-checkbox-icon').trigger('click')
        wrapper.vm.setDirty()

        expect(wrapper.vm.selectedCategories).toStrictEqual(mockedOptions)
        expect(wrapper.emitted()).toHaveProperty('changed')
        expect(wrapper.emitted().changed[0][0]).toStrictEqual(mockedOptions)
    })
})