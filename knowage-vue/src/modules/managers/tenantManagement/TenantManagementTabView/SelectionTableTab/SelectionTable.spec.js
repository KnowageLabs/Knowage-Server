import { mount } from '@vue/test-utils'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import SelectionTable from './SelectionTable.vue'
import Toolbar from 'primevue/toolbar'

const mockedOptions = [
    { ID: 1, LABEL: 'Default Model Category' },
    { ID: 2, LABEL: 'Examples' },
    { ID: 3, LABEL: 'Big Data' },
    { ID: 4, LABEL: 'Sales' }
]

const mockedSelected = [
    { ID: 2, LABEL: 'Examples' },
    { ID: 4, LABEL: 'Sales' }
]

const factory = (dataList, selectedData) => {
    return mount(SelectionTable, {
        props: {
            dataList,
            selectedData
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

        expect(wrapper.props('dataList').length).toBe(0)

        expect(wrapper.find('[data-test="data-table"]').html()).toContain('common.info.noDataFound')
    })
    it('one or more rows are selected if the detail has selections', async () => {
        const wrapper = factory(mockedOptions, mockedSelected)
        const dataTable = wrapper.find('[data-test="data-table"]')

        expect(dataTable.html()).toContain('Examples')
        expect(dataTable.html()).toContain('Sales')

        expect(wrapper.vm.dataList).toStrictEqual(mockedOptions)
        expect(wrapper.vm.selectedData).toStrictEqual(mockedSelected)
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
