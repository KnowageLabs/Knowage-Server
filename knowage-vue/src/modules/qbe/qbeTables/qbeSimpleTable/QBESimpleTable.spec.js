import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import Button from 'primevue/button'
import Column from 'primevue/column'
import Checkbox from 'primevue/checkbox'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import Menu from 'primevue/menu'
import QBESimpleTable from './QBESimpleTable.vue'
import PrimeVue from 'primevue/config'
import Tooltip from 'primevue/tooltip'

const mockedQuery = {
    id: 'q1',
    name: 'Main',
    fields: [
        {
            id: 'it.eng.knowage.inventory.Product:brand_name',
            alias: 'Brand name',
            type: 'datamartField',
            fieldType: 'attribute',
            entity: 'Product',
            field: 'Brand name',
            funct: 'NONE',
            color: '#F46036',
            group: true,
            order: 'NONE',
            include: true,
            inUse: true,
            visible: true,
            iconCls: 'attribute',
            dataType: 'java.lang.String',
            format: '#,###',
            longDescription: 'Product : Brand name',
            distinct: false,
            leaf: true
        },
        {
            id: 'it.eng.knowage.inventory.Product:product_name',
            alias: 'Product name',
            type: 'datamartField',
            fieldType: 'attribute',
            entity: 'Product',
            field: 'Product name',
            funct: 'NONE',
            color: '#F46036',
            group: true,
            order: 'NONE',
            include: true,
            inUse: true,
            visible: true,
            iconCls: 'attribute',
            dataType: 'java.lang.String',
            format: '#,###',
            longDescription: 'Product : Product name',
            distinct: false,
            leaf: true
        }
    ],
    distinct: false,
    filters: [
        {
            filterId: 'Filter1',
            filterDescripion: 'Filter1',
            filterInd: 1,
            promptable: false,
            leftOperandValue: 'it.eng.knowage.inventory.Product:brand_name',
            leftOperandDescription: 'Product : Brand name',
            leftOperandLongDescription: 'Product : Brand name',
            leftOperandType: 'Field Content',
            leftOperandDefaultValue: null,
            leftOperandLastValue: null,
            leftOperandAlias: 'Brand name',
            leftOperandDataType: '',
            operator: 'EQUALS TO',
            rightOperandValue: ['Test'],
            rightOperandDescription: 'Test',
            rightOperandLongDescription: '',
            rightOperandType: 'Static Content',
            rightType: 'manual',
            rightOperandDefaultValue: [''],
            rightOperandLastValue: [''],
            rightOperandAlias: null,
            rightOperandDataType: '',
            booleanConnector: 'AND',
            deleteButton: false,
            color: '#F46036',
            entity: 'Product'
        },
        {
            filterId: 'Filter2',
            filterDescripion: 'Filter2',
            filterInd: 2,
            promptable: false,
            leftOperandValue: 'it.eng.knowage.inventory.Product:brand_name',
            leftOperandDescription: 'Product : Brand name',
            leftOperandLongDescription: 'Product : Brand name',
            leftOperandType: 'Field Content',
            leftOperandDefaultValue: null,
            leftOperandLastValue: null,
            leftOperandAlias: 'Brand name',
            leftOperandDataType: '',
            operator: 'EQUALS TO',
            rightOperandValue: ['Bla'],
            rightOperandDescription: 'Bla',
            rightOperandLongDescription: '',
            rightOperandType: 'Static Content',
            rightType: 'manual',
            rightOperandDefaultValue: [''],
            rightOperandLastValue: [''],
            rightOperandAlias: null,
            rightOperandDataType: '',
            booleanConnector: 'AND',
            deleteButton: false,
            color: '#F46036',
            entity: 'Product'
        },
        {
            filterId: 'Filter3',
            filterDescripion: 'Filter3',
            filterInd: 3,
            promptable: false,
            leftOperandValue: 'it.eng.knowage.inventory.Product:product_name',
            leftOperandDescription: 'Product : Product name',
            leftOperandLongDescription: 'Product : Product name',
            leftOperandType: 'Field Content',
            leftOperandDefaultValue: null,
            leftOperandLastValue: null,
            leftOperandAlias: 'Product name',
            leftOperandDataType: '',
            operator: 'EQUALS TO',
            rightOperandValue: ['dfsfsd'],
            rightOperandDescription: 'dfsfsd',
            rightOperandLongDescription: '',
            rightOperandType: 'Static Content',
            rightType: 'manual',
            rightOperandDefaultValue: [''],
            rightOperandLastValue: [''],
            rightOperandAlias: null,
            rightOperandDataType: '',
            booleanConnector: 'AND',
            deleteButton: false,
            color: '#F46036',
            entity: 'Product'
        },
        {
            filterId: 'Filter4',
            filterDescripion: 'Filter4',
            filterInd: 4,
            promptable: false,
            leftOperandValue: 'it.eng.knowage.inventory.Product:product_name',
            leftOperandDescription: 'Product : Product name',
            leftOperandLongDescription: 'Product : Product name',
            leftOperandType: 'Field Content',
            leftOperandDefaultValue: null,
            leftOperandLastValue: null,
            leftOperandAlias: 'Product name',
            leftOperandDataType: '',
            operator: 'EQUALS TO',
            rightOperandValue: ['fdsdfs'],
            rightOperandDescription: 'fdsdfs',
            rightOperandLongDescription: '',
            rightOperandType: 'Static Content',
            rightType: 'manual',
            rightOperandDefaultValue: [''],
            rightOperandLastValue: [''],
            rightOperandAlias: null,
            rightOperandDataType: '',
            booleanConnector: 'AND',
            deleteButton: false,
            color: '#F46036',
            entity: 'Product'
        }
    ],
    calendar: {},
    expression: {
        type: 'NODE_OP',
        value: 'AND',
        childNodes: [
            {
                type: 'NODE_CONST',
                childNodes: [],
                value: '$F{Filter4}',
                details: {
                    leftOperandAlias: 'Product name',
                    operator: 'EQUALS TO',
                    entity: 'Product',
                    rightOperandValue: 'fdsdfs'
                }
            },
            {
                type: 'NODE_OP',
                value: 'AND',
                childNodes: [
                    {
                        type: 'NODE_CONST',
                        childNodes: [],
                        value: '$F{Filter3}',
                        details: {
                            leftOperandAlias: 'Product name',
                            operator: 'EQUALS TO',
                            entity: 'Product',
                            rightOperandValue: 'dfsfsd'
                        }
                    },
                    {
                        type: 'NODE_OP',
                        childNodes: [
                            {
                                type: 'NODE_CONST',
                                childNodes: [],
                                value: '$F{Filter2}',
                                details: {
                                    leftOperandAlias: 'Brand name',
                                    operator: 'EQUALS TO',
                                    entity: 'Product',
                                    rightOperandValue: 'Bla'
                                }
                            },
                            {
                                type: 'NODE_CONST',
                                childNodes: [],
                                value: '$F{Filter1}',
                                details: {
                                    leftOperandAlias: 'Brand name',
                                    operator: 'EQUALS TO',
                                    entity: 'Product',
                                    rightOperandValue: 'Test'
                                }
                            }
                        ],
                        value: 'AND'
                    }
                ]
            }
        ]
    },
    isNestedExpression: false,
    havings: [],
    graph: [],
    relationRoles: [],
    subqueries: []
}

const factory = (query) => {
    return mount(QBESimpleTable, {
        props: {
            query: query
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
        const wrapper = factory(JSON.parse(JSON.stringify(mockedQuery)))

        expect(wrapper.vm.rows.length).toBe(2)
        expect(wrapper.html()).toContain('Product')
        expect(wrapper.html()).toContain('Brand name')
        expect(wrapper.html()).toContain('Product name')
    })

    it('removes a column when clicking on delete column', async () => {
        const wrapper = factory(JSON.parse(JSON.stringify(mockedQuery)))

        expect(wrapper.vm.rows.length).toBe(2)

        await wrapper.find('[data-test="menu-toggle"]').trigger('click')
        expect(wrapper.vm.menuItems[wrapper.vm.menuItems.length - 1].icon).toBe('pi pi-trash')
        expect(wrapper.vm.menuItems[wrapper.vm.menuItems.length - 1].label).toBe('common.delete')

        expect(wrapper.vm.selectedQuery.fields[0].id).toBe('it.eng.knowage.inventory.Product:brand_name')

        wrapper.vm.deleteColumn(0)

        expect(wrapper.vm.selectedQuery.fields[0].id).not.toBe('it.eng.knowage.inventory.Product:brand_name')
    })

    it('changes column position when reordering columns', () => {
        const wrapper = factory(JSON.parse(JSON.stringify(mockedQuery)))
        const reorderedArray = [{ ...mockedQuery.fields[1] }, { ...mockedQuery.fields[0] }]

        expect(wrapper.vm.rows.length).toBe(2)
        expect(wrapper.vm.selectedQuery.fields[0].id).toBe('it.eng.knowage.inventory.Product:brand_name')

        wrapper.vm.onRowReorder({ value: reorderedArray })

        expect(wrapper.vm.selectedQuery.fields[0].id).toBe('it.eng.knowage.inventory.Product:product_name')
    })
})
