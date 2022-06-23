import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Menu from 'primevue/menu'
import QBEExpandableEntity from './QBEExpandableEntity.vue'
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

const mockedEntities = [
    {
        id: 'it.eng.knowage.inventory.Product::Product',
        text: 'Product',
        iconCls: 'dimension',
        qtip: 'Product',
        attributes: {
            iconCls: 'dimension',
            type: 'entity',
            londDescription: 'Product',
            linkedToWords: false
        },
        children: [
            {
                id: 'it.eng.knowage.inventory.Product:product_id',
                text: 'Product id',
                iconCls: 'attribute',
                dataType: 'java.lang.Integer',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Product id',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Product id',
                    longDescription: 'Product : Product id'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:brand_name',
                text: 'Brand name',
                iconCls: 'attribute',
                dataType: 'java.lang.String',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Brand name',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Brand name',
                    longDescription: 'Product : Brand name'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:product_name',
                text: 'Product name',
                iconCls: 'attribute',
                dataType: 'java.lang.String',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Product name',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Product name',
                    longDescription: 'Product : Product name'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:SKU',
                text: 'SKU',
                iconCls: 'attribute',
                dataType: 'java.lang.Long',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'SKU',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'SKU',
                    longDescription: 'Product : SKU'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:SRP',
                text: 'SRP',
                iconCls: 'attribute',
                dataType: 'java.math.BigDecimal',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'SRP',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'SRP',
                    longDescription: 'Product : SRP'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:gross_weight',
                text: 'Gross weight',
                iconCls: 'attribute',
                dataType: 'java.lang.Double',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Gross weight',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Gross weight',
                    longDescription: 'Product : Gross weight'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:net_weight',
                text: 'Net weight',
                iconCls: 'attribute',
                dataType: 'java.lang.Double',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Net weight',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Net weight',
                    longDescription: 'Product : Net weight'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:recyclable_package',
                text: 'Recyclable package',
                iconCls: 'attribute',
                dataType: 'java.lang.Boolean',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Recyclable package',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Recyclable package',
                    longDescription: 'Product : Recyclable package'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:low_fat',
                text: 'Low fat',
                iconCls: 'attribute',
                dataType: 'java.lang.Boolean',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Low fat',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Low fat',
                    longDescription: 'Product : Low fat'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:units_per_case',
                text: 'Units per case',
                iconCls: 'attribute',
                dataType: 'java.lang.Short',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Units per case',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Units per case',
                    longDescription: 'Product : Units per case'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:cases_per_pallet',
                text: 'Cases per pallet',
                iconCls: 'attribute',
                dataType: 'java.lang.Short',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Cases per pallet',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Cases per pallet',
                    longDescription: 'Product : Cases per pallet'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:shelf_width',
                text: 'Shelf width',
                iconCls: 'attribute',
                dataType: 'java.lang.Double',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Shelf width',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Shelf width',
                    longDescription: 'Product : Shelf width'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:shelf_height',
                text: 'Shelf height',
                iconCls: 'attribute',
                dataType: 'java.lang.Double',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Shelf height',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Shelf height',
                    longDescription: 'Product : Shelf height'
                },
                color: '#F46036'
            },
            {
                id: 'it.eng.knowage.inventory.Product:shelf_depth',
                text: 'Shelf depth',
                iconCls: 'attribute',
                dataType: 'java.lang.Double',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Shelf depth',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product',
                    field: 'Shelf depth',
                    longDescription: 'Product : Shelf depth'
                },
                color: '#F46036'
            }
        ],
        relation: [
            {
                id: 'pcid-it.eng.knowage.inventory.Product_class::Product_class-it.eng.knowage.inventory.Product::Product',
                text: '-->Product class',
                iconCls: 'relation',
                leaf: true,
                qtip: 'Relation name: pcid<br>Source fields: [product_class_id]<br>Target entity: Product class<br>Target fields: product_class_id<br>',
                relationName: 'pcid',
                sourceFields: 'product_class_id',
                targetEntity: 'it.eng.knowage.inventory.Product_class::Product_class',
                targetEntityLabel: 'Product class',
                targetFields: 'product_class_id',
                joinType: 'INNER',
                isConsidered: true,
                attributes: {
                    iconCls: 'relation',
                    type: 'relation',
                    sourceEntity: 'Product',
                    entity: 'Product class',
                    field: 'Product class',
                    longDescription: 'Product class'
                }
            }
        ],
        color: '#F46036',
        expanded: false
    },
    {
        id: 'it.eng.knowage.inventory.Product_class::Product_class',
        text: 'Product class',
        iconCls: 'dimension',
        qtip: 'Product class',
        attributes: {
            iconCls: 'dimension',
            type: 'entity',
            londDescription: 'Product class',
            linkedToWords: false
        },
        children: [
            {
                id: 'it.eng.knowage.inventory.Product_class:product_class_id',
                text: 'Product class id',
                iconCls: 'attribute',
                dataType: 'java.lang.Integer',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Product class id',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product class',
                    field: 'Product class id',
                    longDescription: 'Product class : Product class id'
                },
                color: '#2E294E'
            },
            {
                id: 'it.eng.knowage.inventory.Product_class:product_subcategory',
                text: 'Product subcategory',
                iconCls: 'attribute',
                dataType: 'java.lang.String',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Product subcategory',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product class',
                    field: 'Product subcategory',
                    longDescription: 'Product class : Product subcategory'
                },
                color: '#2E294E'
            },
            {
                id: 'it.eng.knowage.inventory.Product_class:product_category',
                text: 'Product category',
                iconCls: 'attribute',
                dataType: 'java.lang.String',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Product category',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product class',
                    field: 'Product category',
                    longDescription: 'Product class : Product category'
                },
                color: '#2E294E'
            },
            {
                id: 'it.eng.knowage.inventory.Product_class:product_department',
                text: 'Product department',
                iconCls: 'attribute',
                dataType: 'java.lang.String',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Product department',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product class',
                    field: 'Product department',
                    longDescription: 'Product class : Product department'
                },
                color: '#2E294E'
            },
            {
                id: 'it.eng.knowage.inventory.Product_class:product_family',
                text: 'Product family',
                iconCls: 'attribute',
                dataType: 'java.lang.String',
                aggtype: 'SUM',
                format: '#,###',
                leaf: true,
                qtip: 'Product family',
                attributes: {
                    iconCls: 'attribute',
                    type: 'field',
                    entity: 'Product class',
                    field: 'Product family',
                    longDescription: 'Product class : Product family'
                },
                color: '#2E294E'
            }
        ],
        relation: [],
        color: '#2E294E',
        expanded: false
    }
]

const factory = (query, entities) => {
    return mount(QBEExpandableEntity, {
        props: {
            query: query,
            availableEntities: entities
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue, createTestingPinia()],
            stubs: {
                Button,
                InputText,
                Menu,
                Tooltip
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('QBE Expandable Entity', () => {
    it('adds a field to the list when clicking on a field in the list', async () => {
        const wrapper = factory(mockedQuery, mockedEntities)

        await wrapper.find('[data-test="entity-it.eng.knowage.inventory.Product_class::Product_class"]').trigger('click')
        expect(wrapper.emitted()).toHaveProperty('entityChildClicked')
        expect(wrapper.emitted()['entityChildClicked'][0][0]).toStrictEqual({
            id: 'it.eng.knowage.inventory.Product_class:product_class_id',
            text: 'Product class id',
            iconCls: 'attribute',
            dataType: 'java.lang.Integer',
            aggtype: 'SUM',
            format: '#,###',
            leaf: true,
            qtip: 'Product class id',
            attributes: {
                iconCls: 'attribute',
                type: 'field',
                entity: 'Product class',
                field: 'Product class id',
                longDescription: 'Product class : Product class id'
            },
            color: '#F46036'
        })
    })

    it('shows a filter icon if one of the fields from the list has a filter', async () => {
        const wrapper = factory(mockedQuery, mockedEntities)

        expect(wrapper.find('[data-test="child-it.eng.knowage.inventory.Product:brand_name"]').html()).toContain('qbe-active-filter-icon')
    })

    it('expands parent entity on clicking', async () => {
        const wrapper = factory(mockedQuery, mockedEntities)

        await wrapper.find('[data-test="expand-it.eng.knowage.inventory.Product_class::Product_class"]').trigger('click')
        expect(mockedEntities[1].expanded).toBe(true)

        await wrapper.find('[data-test="expand-it.eng.knowage.inventory.Product_class::Product_class"]').trigger('click')
        expect(mockedEntities[1].expanded).toBe(false)
    })
})
