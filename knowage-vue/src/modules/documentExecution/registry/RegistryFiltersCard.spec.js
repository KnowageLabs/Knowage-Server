import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import RegistryFiltersCard from './RegistryFiltersCard.vue'
import Toolbar from 'primevue/toolbar'

const mockedFilters = [
    {
        column: {
            field: 'store_type',
            editorType: 'COMBO',
            isEditable: true,
            title: 'store type',
            infoColumn: false,
            columnInfo: { dataIndex: 'column_3', defaultValue: null, header: 'store_type', multiValue: false, name: 'column_3', type: 'string' },
            isVisible: true,
            unsigned: false
        },
        field: 'store_type',
        presentation: 'MANUAL',
        static: false,
        title: 'Store type',
        visible: true
    },
    {
        column: {
            field: 'sales_city',
            subEntity: 'rel_region_id_in_region',
            foreignKey: 'rel_region_id_in_region',
            editorType: 'COMBO',
            isEditable: true,
            columnInfo: { dataIndex: 'column_11', defaultValue: null, header: 'sales_city', multiValue: false, name: 'column_11', type: 'string' },
            isVisible: true,
            title: 'Foreign key - city',
            unsigned: false
        },
        field: 'sales_city',
        presentation: 'COMBO',
        static: false,
        title: 'Sales city',
        visible: true
    }
]

const factory = (filters) => {
    return mount(RegistryFiltersCard, {
        props: {
            propFilters: filters,
            entity: 'it.eng.knowage.meta.stores_for_registry.Store',
            id: '1'
        },
        global: {
            plugins: [],
            stubs: {
                Button,
                InputText,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Registry loading', () => {
    it('allows filter button when no filter are present', () => {
        const wrapper = factory([])

        expect(wrapper.vm.filters.length).toBe(0)
        expect(wrapper.find('[data-test="filter-button"]').exists()).toBe(true)
    })
    it('emits filter event when a filter is set and the button is clicked', async () => {
        const wrapper = factory(mockedFilters)

        expect(wrapper.vm.filters.length).toBe(2)

        wrapper.vm.setFilterValue('Test', 0)
        wrapper.vm.setFilterValue('Mock Test', 1)

        await wrapper.find('[data-test="filter-button"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('filter')
        expect(wrapper.emitted()['filter'][0][0][0].filterValue).toBe('Test')
        expect(wrapper.emitted()['filter'][0][0][1].filterValue).toBe('Mock Test')
    })
})
