import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import RegistryDatatable from './RegistryDatatable.vue'
import Toolbar from 'primevue/toolbar'

const mockedColumns = [
    {
        field: 'store_name',
        editorType: 'TEXT',
        isEditable: true,
        title: 'store name',
        infoColumn: false,
        isVisible: true,
        sorter: 'ASC',
        unsigned: false,
        columnInfo: {
            name: 'column_2',
            header: 'store_name',
            dataIndex: 'column_2',
            type: 'string',
            multiValue: false,
            defaultValue: null
        }
    },
    {
        field: 'store_type',
        editorType: 'COMBO',
        isEditable: true,
        title: 'store type',
        infoColumn: false,
        isVisible: true,
        unsigned: false,
        columnInfo: {
            name: 'column_3',
            header: 'store_type',
            dataIndex: 'column_3',
            type: 'string',
            multiValue: false,
            defaultValue: null
        }
    },
    {
        field: 'store_city',
        editorType: 'TEXT',
        isEditable: false,
        title: 'store city',
        infoColumn: false,
        isVisible: true,
        unsigned: false,
        columnInfo: {
            name: 'column_4',
            header: 'store_city',
            dataIndex: 'column_4',
            type: 'string',
            multiValue: false,
            defaultValue: null
        }
    },
    {
        field: 'store_state',
        editorType: 'TEXT',
        isEditable: false,
        title: 'store state',
        infoColumn: false,
        isVisible: true,
        unsigned: false,
        columnInfo: {
            name: 'column_5',
            header: 'store_state',
            dataIndex: 'column_5',
            type: 'string',
            multiValue: false,
            defaultValue: null
        }
    },
    {
        field: 'florist',
        editorType: 'TEXT',
        isEditable: true,
        title: 'has florist',
        infoColumn: false,
        isVisible: true,
        unsigned: false,
        columnInfo: {
            name: 'column_6',
            header: 'florist',
            dataIndex: 'column_6',
            type: 'boolean',
            multiValue: false,
            defaultValue: null
        }
    },
    {
        field: 'coffee_bar',
        editorType: 'TEXT',
        isEditable: true,
        title: 'has coffee bar',
        infoColumn: false,
        isVisible: true,
        unsigned: false,
        columnInfo: {
            name: 'column_7',
            header: 'coffee_bar',
            dataIndex: 'column_7',
            type: 'boolean',
            multiValue: false,
            defaultValue: null
        }
    },
    {
        field: 'video_store',
        editorType: 'TEXT',
        isEditable: false,
        title: 'has video store',
        infoColumn: false,
        isVisible: true,
        unsigned: false,
        columnInfo: {
            name: 'column_8',
            header: 'video_store',
            dataIndex: 'column_8',
            type: 'boolean',
            multiValue: false,
            defaultValue: null
        }
    },
    {
        field: 'first_opened_date',
        editorType: 'TEXT',
        isEditable: true,
        title: 'first opened date',
        infoColumn: false,
        isVisible: true,
        unsigned: false,
        columnInfo: {
            name: 'column_9',
            header: 'first_opened_date',
            dataIndex: 'column_9',
            type: 'timestamp',
            dateFormat: 'd/m/Y H:i:s.uuu',
            dateFormatJava: 'dd/MM/yyyy HH:mm:ss.SSS',
            multiValue: false,
            defaultValue: null
        }
    },
    {
        field: 'store_sqft',
        editorType: 'TEXT',
        isEditable: true,
        title: 'store square foot',
        infoColumn: false,
        isVisible: true,
        unsigned: false,
        columnInfo: {
            name: 'column_10',
            header: 'store_sqft',
            dataIndex: 'column_10',
            type: 'int',
            precision: 0,
            scale: 0,
            format: '#,###',
            multiValue: false,
            defaultValue: null
        }
    },
    {
        field: 'sales_city',
        subEntity: 'rel_region_id_in_region',
        foreignKey: 'rel_region_id_in_region',
        editorType: 'COMBO',
        isEditable: true,
        title: 'Foreign key - city',
        infoColumn: false,
        isVisible: true,
        unsigned: false,
        columnInfo: {
            name: 'column_11',
            header: 'sales_city',
            dataIndex: 'column_11',
            type: 'string',
            multiValue: false,
            defaultValue: null
        }
    }
]
const mockedRows = [
    {
        id: 1,
        store_id: 91,
        store_name: 'Bojan',
        store_type: 'Supermarket',
        store_city: '',
        store_state: '',
        florist: true,
        coffee_bar: false,
        video_store: false,
        first_opened_date: '12-25-1995',
        store_sqft: 35345,
        sales_city: 'Colma'
    },
    {
        id: 2,
        store_id: 57,
        store_name: 'Hmm',
        store_type: 'Supermarket',
        store_city: '',
        store_state: '',
        florist: true,
        coffee_bar: true,
        video_store: false,
        first_opened_date: '12-25-1995',
        store_sqft: 241241,
        sales_city: 'Altadena'
    },
    {
        id: 3,
        store_id: 11,
        store_name: 'Store 11',
        store_type: 'Supermarket',
        store_city: 'Portland',
        store_state: 'OR',
        florist: false,
        coffee_bar: true,
        video_store: false,
        first_opened_date: '12-25-1995',
        store_sqft: 20319,
        sales_city: 'Guadalajara'
    },
    {
        id: 4,
        store_id: 1,
        store_name: 'Store 11',
        store_type: 'Supermarket',
        store_city: 'Salem',
        store_state: 'OR',
        florist: true,
        coffee_bar: false,
        video_store: false,
        first_opened_date: '12-25-1995',
        store_sqft: 30251,
        sales_city: 'Acapulco'
    },
    {
        id: 5,
        store_id: 12,
        store_name: 'Store 124',
        store_type: 'Deluxe Supermarket',
        store_city: 'Hidalgo',
        store_state: 'Zacatecas',
        florist: true,
        coffee_bar: true,
        video_store: true,
        first_opened_date: '12-25-1995',
        store_sqft: 30584,
        sales_city: 'Hidalgo'
    }
]
const mockedColumnMap = {
    id: 'id',
    column_1: 'store_id',
    column_2: 'store_name',
    column_3: 'store_type',
    column_4: 'store_city',
    column_5: 'store_state',
    column_6: 'florist',
    column_7: 'coffee_bar',
    column_8: 'video_store',
    column_9: 'first_opened_date',
    column_10: 'store_sqft',
    column_11: 'sales_city'
}
const mockedConfiguration = [
    {
        name: 'enableButtons',
        value: 'true'
    }
]

const factory = (rows) => {
    return mount(RegistryDatatable, {
        props: {
            propColumns: mockedColumns,
            propRows: rows,
            columnMap: mockedColumnMap,
            propConfiguration: mockedConfiguration,
            entity: 'it.eng.knowage.meta.stores_for_registry.Store',
            id: '1'
        },
        global: {
            plugins: [],
            stubs: {
                Button,
                Calendar: true,
                Card,
                Checkbox,
                Column,
                DataTable,
                KnFabButton,
                Dropdown,
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
    it('the entries list shows an hint when loaded empty', () => {
        const wrapper = factory([])

        expect(wrapper.html()).toContain('common.info.noDataFound')
    })
    it('shows a pen icon on the editable fields', () => {
        const wrapper = factory(mockedRows)

        expect(wrapper.find('[data-test="store_name-icon"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="store_type-icon"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="store_city-icon"]').exists()).toBe(false)
        expect(wrapper.find('[data-test="store_state-icon"]').exists()).toBe(false)
        expect(wrapper.find('[data-test="first_opened_date-icon"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="sales_city-icon"]').exists()).toBe(true)
    })
    it("adds a new row on top when the '+' button is clicked", async () => {
        const wrapper = factory(mockedRows)

        expect(wrapper.vm.rows.length).toBe(5)

        await wrapper.find('[data-test="new-row-button"]').trigger('click')

        expect(wrapper.vm.rows[0]).toStrictEqual({ id: 5, isNew: true, store_name: '', store_type: '', store_city: '', store_state: '', florist: '', coffee_bar: '', video_store: '', first_opened_date: '', store_sqft: '', sales_city: '' })
        expect(wrapper.vm.rows.length).toBe(6)
    })
    it('allows editing when clicking on an editable field', async () => {
        const wrapper = factory(mockedRows)

        expect(wrapper.vm.columns[1].field).toBe('store_name')
        expect(wrapper.find('[data-test="store_name-editor"]').exists()).toBe(false)
        await wrapper.find('[data-test="store_name-body"]').trigger('click')
        expect(wrapper.find('[data-test="store_name-editor"]').exists()).toBe(true)

        expect(wrapper.vm.columns[2].field).toBe('store_type')
        expect(wrapper.find('[data-test="store_type-editor"]').exists()).toBe(false)
        await wrapper.find('[data-test="store_type-body"]').trigger('click')
        expect(wrapper.find('[data-test="store_type-editor"]').exists()).toBe(true)

        expect(wrapper.vm.columns[8].field).toBe('first_opened_date')
        expect(wrapper.find('[data-test="first_opened_date-editor"]').exists()).toBe(false)
        await wrapper.find('[data-test="first_opened_date-body"]').trigger('click')
        expect(wrapper.find('[data-test="first_opened_date-editor"]').exists()).toBe(true)
    })
    it('shows a checkbox if the editable field is boolean type', () => {
        const wrapper = factory(mockedRows)

        expect(wrapper.vm.columns[5].field).toBe('florist')
        expect(wrapper.vm.columns[5].columnInfo.type).toBe('boolean')
        expect(wrapper.find('[data-test="florist-body"]').html()).toContain('p-checkbox')
    })
    it('shows a text input if the editable field is not set', async () => {
        const wrapper = factory(mockedRows)

        expect(wrapper.vm.columns[1].field).toBe('store_name')
        expect(wrapper.vm.columns[1].columnInfo.type).toBe('string')

        await wrapper.find('[data-test="store_name-body"]').trigger('click')
        expect(wrapper.find('[data-test="store_name-editor"]').html()).toContain('p-inputtext')
    })
    it('shows a select input if the editable field is multiple', async () => {
        const wrapper = factory(mockedRows)

        expect(wrapper.vm.columns[2].field).toBe('store_type')
        expect(wrapper.vm.columns[2].editorType).toBe('COMBO')
        expect(wrapper.vm.columns[2].columnInfo.type).toBe('string')

        await wrapper.find('[data-test="store_type-body"]').trigger('click')
        expect(wrapper.find('[data-test="store_type-editor"]').html()).toContain('p-dropdown')
    })
    it('shows a datepicker if the editable field is date type', async () => {
        const wrapper = factory(mockedRows)

        expect(wrapper.vm.columns[8].field).toBe('first_opened_date')
        expect(wrapper.vm.columns[8].columnInfo.type).toBe('timestamp')
    })
})
