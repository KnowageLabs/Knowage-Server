import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Column from 'primevue/column'
import Checkbox from 'primevue/checkbox'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import Menu from 'primevue/menu'
import QBESmartTable from './QBESmartTable.vue'
import PrimeVue from 'primevue/config'
import Tooltip from 'primevue/tooltip'
import Toolbar from 'primevue/toolbar'

const mockedQuery = {
    id: 'q1',
    name: 'Main',
    fields: [
        {
            id: 'it.eng.knowage.meta.Store:store_type',
            alias: 'Store type',
            type: 'datamartField',
            fieldType: 'attribute',
            entity: 'Store',
            field: 'Store type',
            funct: 'NONE',
            color: '#C5D86D',
            group: true,
            order: 'NONE',
            include: true,
            inUse: true,
            visible: true,
            iconCls: 'attribute',
            dataType: 'java.lang.String',
            format: '#,###',
            longDescription: 'Store : Store type',
            distinct: false,
            leaf: true
        },
        {
            id: 'it.eng.knowage.meta.Inventory_fact:compId.warehouse_id',
            alias: 'Warehouse id',
            type: 'datamartField',
            fieldType: 'attribute',
            entity: 'Inventory fact',
            field: 'Warehouse id',
            funct: 'NONE',
            color: '#D7263D',
            group: true,
            order: 'NONE',
            include: true,
            inUse: true,
            visible: true,
            iconCls: 'attribute',
            dataType: 'java.lang.Integer',
            format: '#,###',
            longDescription: 'Inventory fact : Warehouse id',
            distinct: false,
            leaf: true
        }
    ],
    distinct: false,
    filters: [],
    calendar: {},
    expression: {},
    isNestedExpression: false,
    havings: [],
    graph: [],
    relationRoles: [],
    subqueries: [
        {
            id: 'q2',
            name: 'subentity-q2',
            fields: [
                {
                    id: 'it.eng.knowage.meta.Warehouse_class:warehouse_class_id',
                    alias: 'Warehouse class id',
                    type: 'datamartField',
                    fieldType: 'attribute',
                    entity: 'Warehouse class',
                    field: 'Warehouse class id',
                    funct: 'NONE',
                    color: '#009688',
                    group: true,
                    order: 'NONE',
                    include: true,
                    inUse: true,
                    visible: true,
                    iconCls: 'attribute',
                    dataType: 'java.lang.Integer',
                    format: '#,###',
                    longDescription: 'Warehouse class : Warehouse class id',
                    distinct: false,
                    leaf: true
                }
            ],
            distinct: false,
            filters: [],
            calendar: {},
            expression: {},
            isNestedExpression: false,
            havings: [],
            graph: [],
            relationRoles: [],
            subqueries: []
        }
    ]
}

const mockedPreview = {
    metaData: {
        totalProperty: 'results',
        root: 'rows',
        id: 'id',
        fields: [
            'recNo',
            {
                name: 'column_1',
                header: 'Store type',
                dataIndex: 'column_1',
                type: 'string',
                multiValue: false
            },
            {
                name: 'column_2',
                header: 'Warehouse id',
                dataIndex: 'column_2',
                type: 'int',
                precision: 0,
                scale: 0,
                multiValue: false
            }
        ]
    },
    results: 24,
    rows: [
        {
            id: 1,
            column_1: 'Supermarket',
            column_2: 1
        },
        {
            id: 2,
            column_1: 'Small Grocery',
            column_2: 2
        },
        {
            id: 3,
            column_1: 'Supermarket',
            column_2: 3
        },
        {
            id: 4,
            column_1: 'Gourmet Supermarket',
            column_2: 4
        },
        {
            id: 5,
            column_1: 'Small Grocery',
            column_2: 5
        },
        {
            id: 6,
            column_1: 'Gourmet Supermarket',
            column_2: 6
        },
        {
            id: 7,
            column_1: 'Supermarket',
            column_2: 7
        },
        {
            id: 8,
            column_1: 'Deluxe Supermarket',
            column_2: 8
        },
        {
            id: 9,
            column_1: 'Mid-Size Grocery',
            column_2: 9
        },
        {
            id: 10,
            column_1: 'Supermarket',
            column_2: 10
        },
        {
            id: 11,
            column_1: 'Supermarket',
            column_2: 11
        },
        {
            id: 12,
            column_1: 'Deluxe Supermarket',
            column_2: 12
        },
        {
            id: 13,
            column_1: 'Deluxe Supermarket',
            column_2: 13
        },
        {
            id: 14,
            column_1: 'Small Grocery',
            column_2: 14
        },
        {
            id: 15,
            column_1: 'Supermarket',
            column_2: 15
        },
        {
            id: 16,
            column_1: 'Supermarket',
            column_2: 16
        },
        {
            id: 17,
            column_1: 'Deluxe Supermarket',
            column_2: 17
        },
        {
            id: 18,
            column_1: 'Mid-Size Grocery',
            column_2: 18
        },
        {
            id: 19,
            column_1: 'Deluxe Supermarket',
            column_2: 19
        },
        {
            id: 20,
            column_1: 'Mid-Size Grocery',
            column_2: 20
        },
        {
            id: 21,
            column_1: 'Deluxe Supermarket',
            column_2: 21
        },
        {
            id: 22,
            column_1: 'Small Grocery',
            column_2: 22
        },
        {
            id: 23,
            column_1: 'Mid-Size Grocery',
            column_2: 23
        },
        {
            id: 24,
            column_1: 'Supermarket',
            column_2: 24
        }
    ]
}

const mockedPagination = {
    start: 0,
    limit: 25,
    size: 24
}

const factory = (query, previewData, pagination) => {
    return mount(QBESmartTable, {
        props: {
            query: query,
            previewData: previewData,
            pagination: pagination
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Button,
                Checkbox,
                Column,
                DataTable,
                Dropdown,
                InputText,
                Menu,
                Toolbar,
                Tooltip,
                routerLink: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('QBE Simple Table', () => {
    it('shows a query if some columns are set', () => {
        const wrapper = factory(JSON.parse(JSON.stringify(mockedQuery)), JSON.parse(JSON.stringify(mockedPreview)), JSON.parse(JSON.stringify(mockedPagination)))

        expect(wrapper.html()).toContain('Store type')
        expect(wrapper.html()).toContain('Warehouse id')
    })

    it('removes a column when clicking on delete column', async () => {
        const wrapper = factory(JSON.parse(JSON.stringify(mockedQuery)), JSON.parse(JSON.stringify(mockedPreview)), JSON.parse(JSON.stringify(mockedPagination)))

        expect(wrapper.vm.filteredVisibleFields.length).toBe(2)

        await wrapper.find('[data-test="delete-column-Warehouse id"]').trigger('click')
        expect(wrapper.emitted()).toHaveProperty('removeFieldFromQuery')
        expect(wrapper.emitted('removeFieldFromQuery')[0][0]).toStrictEqual(1)
    })

    it('shows a filter icon on a column if it uses a filter', () => {
        const wrapper = factory(JSON.parse(JSON.stringify(mockedQuery)), JSON.parse(JSON.stringify(mockedPreview)), JSON.parse(JSON.stringify(mockedPagination)))

        expect(wrapper.html()).toContain('fas fa-filter')
    })

    it('change table sorting when clicking on a table header sort button', async () => {
        const wrapper = factory(JSON.parse(JSON.stringify(mockedQuery)), JSON.parse(JSON.stringify(mockedPreview)), JSON.parse(JSON.stringify(mockedPagination)))

        expect(wrapper.html()).toContain('fas fa-sort')

        expect(wrapper.vm.filteredVisibleFields[0].order).toBe('NONE')

        await wrapper.find('[data-test="change-order-Store type"]').trigger('click')
        expect(wrapper.emitted()).toHaveProperty('orderChanged')
        expect(wrapper.vm.filteredVisibleFields[0].order).toBe('ASC')

        await wrapper.find('[data-test="change-order-Store type"]').trigger('click')
        expect(wrapper.emitted()).toHaveProperty('orderChanged')
        expect(wrapper.vm.filteredVisibleFields[0].order).toBe('DESC')
    })
})
