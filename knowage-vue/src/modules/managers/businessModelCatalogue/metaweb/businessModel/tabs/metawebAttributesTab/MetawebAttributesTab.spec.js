import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import MetawebAttributesTab from './MetawebAttributesTab.vue'

const mockedBusinessModel = {
    name: 'Product class',
    uniqueName: 'product_class',
    properties: [
        {
            'structural.visible': {
                propertyType: {
                    id: 'structural.visible',
                    name: 'Visible',
                    description: 'Specify if this column is visible in the query editor',
                    admissibleValues: ['true', 'false'],
                    defaultValue: 'true'
                },
                value: 'true'
            }
        },
        {
            'structural.tabletype': {
                propertyType: {
                    id: 'structural.tabletype',
                    name: 'Type',
                    description: 'The role played by this table in the data model (generic, cube or dimension)',
                    admissibleValues: ['generic', 'cube', 'dimension', 'geographic dimension'],
                    defaultValue: 'generic'
                },
                value: 'dimension'
            }
        }
    ],
    columns: [
        {
            id: null,
            name: 'Product class id',
            uniqueName: 'product_class_id',
            description: null,
            properties: [
                {
                    'structural.visible': {
                        propertyType: {
                            id: 'structural.visible',
                            name: 'Visible',
                            description: 'Specify if this column is visible in the query editor',
                            admissibleValues: ['true', 'false'],
                            defaultValue: 'true'
                        },
                        value: 'true'
                    }
                },
                {
                    'structural.columntype': {
                        propertyType: {
                            id: 'structural.columntype',
                            name: 'Type',
                            description: 'The role played by this column in the data model (measure or attribute)',
                            admissibleValues: ['measure', 'attribute'],
                            defaultValue: 'attribute'
                        },
                        value: 'attribute'
                    }
                },
                {
                    'structural.aggtype': {
                        propertyType: {
                            id: 'structural.aggtype',
                            name: 'Aggregation Type',
                            description: 'The preferred aggregation type for the give column (COUNT, SUM, AVG, MAX, MIN)',
                            admissibleValues: ['COUNT', 'SUM', 'AVG', 'MAX', 'MIN', 'DISTINCT-COUNT'],
                            defaultValue: 'SUM'
                        },
                        value: 'SUM'
                    }
                },
                {
                    'structural.format': {
                        propertyType: {
                            id: 'structural.format',
                            name: 'Format String',
                            description: 'The numeric format to use if the value is numeric',
                            admissibleValues: ['#,###', '#,###.0', '#,###.00', '#,###.000', '#,###.0000', '#,###.00000', '#.###', '$#,##0.00', '€#,##0.00'],
                            defaultValue: '#,###'
                        },
                        value: '#,###'
                    }
                },
                {
                    'structural.attribute': {
                        propertyType: {
                            id: 'structural.attribute',
                            name: 'Profile attribute',
                            description: 'A profile attribute used to filter',
                            admissibleValues: [],
                            defaultValue: ''
                        },
                        value: ''
                    }
                },
                {
                    'structural.datatype': {
                        propertyType: {
                            id: 'structural.datatype',
                            name: 'Data Type',
                            description: 'The data type of the given column (VARCHAR, INTEGER, DOUBLE, ...)',
                            admissibleValues: ['VARCHAR', 'INTEGER', 'DOUBLE', 'DATE', 'TIMESTAMP', 'DECIMAL', 'BIGINT', 'FLOAT', 'SMALLINT', 'TIME', 'BOOLEAN'],
                            defaultValue: 'VARCHAR'
                        },
                        value: 'INTEGER'
                    }
                },
                {
                    'behavioural.notEnabledRoles': {
                        propertyType: {
                            id: 'behavioural.notEnabledRoles',
                            name: 'Roles NOT enabled',
                            description: 'Roles NOT enabled to view this column',
                            admissibleValues: [],
                            defaultValue: ''
                        },
                        value: ''
                    }
                },
                {
                    'physical.physicaltable': {
                        propertyType: {
                            id: 'physical.physicaltable',
                            name: 'Physical table',
                            description: 'The original physical table of this column',
                            admissibleValues: [],
                            defaultValue: 'customer'
                        },
                        value: 'product_class'
                    }
                },
                {
                    'structural.filtercondition': {
                        propertyType: {
                            id: 'structural.filtercondition',
                            name: 'Profile Attribute Filter Type',
                            description: 'The type of filter to use with profile attributes',
                            admissibleValues: ['EQUALS TO', 'IN', 'LIKE'],
                            defaultValue: 'EQUALS TO'
                        },
                        value: 'EQUALS TO'
                    }
                },
                {
                    'structural.customFunction': {
                        propertyType: {
                            id: 'structural.customFunction',
                            name: 'Custom function',
                            description: 'Custom DB function to apply to column',
                            admissibleValues: [],
                            defaultValue: ''
                        },
                        value: ''
                    }
                },
                {
                    'structural.dateformat': {
                        propertyType: {
                            id: 'structural.dateformat',
                            name: 'Format Date',
                            description: 'The date format to use if the value is date',
                            admissibleValues: ['LLLL', 'llll', 'LLL', 'lll', 'DD/MM/YYYY HH:mm:SS', 'DD/MM/YYYY HH:mm', 'LL', 'll', 'L', 'l'],
                            defaultValue: 'LLLL'
                        },
                        value: 'LLLL'
                    }
                },
                {
                    'structural.timeformat': {
                        propertyType: {
                            id: 'structural.timeformat',
                            name: 'Format Time',
                            description: 'The date format to use if the value is time',
                            admissibleValues: ['LT', 'LTS'],
                            defaultValue: 'LT'
                        },
                        value: 'LT'
                    }
                }
            ],
            physicalColumn: {
                id: null,
                name: 'product_class_id',
                uniqueName: null,
                description: null,
                properties: [
                    {
                        'structural.deleted': {
                            propertyType: {
                                id: 'structural.deleted',
                                name: 'Deleted',
                                description: 'Specify if this column was deleted after a db update',
                                admissibleValues: [],
                                defaultValue: 'false'
                            },
                            value: 'false'
                        }
                    }
                ],
                comment: '',
                dataType: 'INTEGER',
                typeName: 'INT',
                size: 10,
                octectLength: 0,
                decimalDigits: 0,
                radix: 10,
                defaultValue: null,
                nullable: false,
                position: 1,
                markedDeleted: false,
                tableName: 'product_class',
                partOfCompositePrimaryKey: false,
                primaryKey: true
            },
            filteredByProfileAttribute: false,
            filteredByRoleVisibility: false,
            identifier: true,
            partOfCompositeIdentifier: false
        },
        {
            id: null,
            name: 'Product subcategory',
            uniqueName: 'product_subcategory',
            description: null,
            properties: [
                {
                    'structural.visible': {
                        propertyType: {
                            id: 'structural.visible',
                            name: 'Visible',
                            description: 'Specify if this column is visible in the query editor',
                            admissibleValues: ['true', 'false'],
                            defaultValue: 'true'
                        },
                        value: 'true'
                    }
                },
                {
                    'structural.columntype': {
                        propertyType: {
                            id: 'structural.columntype',
                            name: 'Type',
                            description: 'The role played by this column in the data model (measure or attribute)',
                            admissibleValues: ['measure', 'attribute'],
                            defaultValue: 'attribute'
                        },
                        value: 'attribute'
                    }
                },
                {
                    'structural.aggtype': {
                        propertyType: {
                            id: 'structural.aggtype',
                            name: 'Aggregation Type',
                            description: 'The preferred aggregation type for the give column (COUNT, SUM, AVG, MAX, MIN)',
                            admissibleValues: ['COUNT', 'SUM', 'AVG', 'MAX', 'MIN', 'DISTINCT-COUNT'],
                            defaultValue: 'SUM'
                        },
                        value: 'SUM'
                    }
                },
                {
                    'structural.format': {
                        propertyType: {
                            id: 'structural.format',
                            name: 'Format String',
                            description: 'The numeric format to use if the value is numeric',
                            admissibleValues: ['#,###', '#,###.0', '#,###.00', '#,###.000', '#,###.0000', '#,###.00000', '#.###', '$#,##0.00', '€#,##0.00'],
                            defaultValue: '#,###'
                        },
                        value: '#,###'
                    }
                },
                {
                    'structural.attribute': {
                        propertyType: {
                            id: 'structural.attribute',
                            name: 'Profile attribute',
                            description: 'A profile attribute used to filter',
                            admissibleValues: [],
                            defaultValue: ''
                        },
                        value: ''
                    }
                },
                {
                    'structural.datatype': {
                        propertyType: {
                            id: 'structural.datatype',
                            name: 'Data Type',
                            description: 'The data type of the given column (VARCHAR, INTEGER, DOUBLE, ...)',
                            admissibleValues: ['VARCHAR', 'INTEGER', 'DOUBLE', 'DATE', 'TIMESTAMP', 'DECIMAL', 'BIGINT', 'FLOAT', 'SMALLINT', 'TIME', 'BOOLEAN'],
                            defaultValue: 'VARCHAR'
                        },
                        value: 'VARCHAR'
                    }
                },
                {
                    'behavioural.notEnabledRoles': {
                        propertyType: {
                            id: 'behavioural.notEnabledRoles',
                            name: 'Roles NOT enabled',
                            description: 'Roles NOT enabled to view this column',
                            admissibleValues: [],
                            defaultValue: ''
                        },
                        value: ''
                    }
                },
                {
                    'physical.physicaltable': {
                        propertyType: {
                            id: 'physical.physicaltable',
                            name: 'Physical table',
                            description: 'The original physical table of this column',
                            admissibleValues: [],
                            defaultValue: 'customer'
                        },
                        value: 'product_class'
                    }
                },
                {
                    'structural.filtercondition': {
                        propertyType: {
                            id: 'structural.filtercondition',
                            name: 'Profile Attribute Filter Type',
                            description: 'The type of filter to use with profile attributes',
                            admissibleValues: ['EQUALS TO', 'IN', 'LIKE'],
                            defaultValue: 'EQUALS TO'
                        },
                        value: 'EQUALS TO'
                    }
                },
                {
                    'structural.customFunction': {
                        propertyType: {
                            id: 'structural.customFunction',
                            name: 'Custom function',
                            description: 'Custom DB function to apply to column',
                            admissibleValues: [],
                            defaultValue: ''
                        },
                        value: ''
                    }
                },
                {
                    'structural.dateformat': {
                        propertyType: {
                            id: 'structural.dateformat',
                            name: 'Format Date',
                            description: 'The date format to use if the value is date',
                            admissibleValues: ['LLLL', 'llll', 'LLL', 'lll', 'DD/MM/YYYY HH:mm:SS', 'DD/MM/YYYY HH:mm', 'LL', 'll', 'L', 'l'],
                            defaultValue: 'LLLL'
                        },
                        value: 'LLLL'
                    }
                },
                {
                    'structural.timeformat': {
                        propertyType: {
                            id: 'structural.timeformat',
                            name: 'Format Time',
                            description: 'The date format to use if the value is time',
                            admissibleValues: ['LT', 'LTS'],
                            defaultValue: 'LT'
                        },
                        value: 'LT'
                    }
                }
            ],
            physicalColumn: {
                id: null,
                name: 'product_subcategory',
                uniqueName: null,
                description: null,
                properties: [
                    {
                        'structural.deleted': {
                            propertyType: {
                                id: 'structural.deleted',
                                name: 'Deleted',
                                description: 'Specify if this column was deleted after a db update',
                                admissibleValues: [],
                                defaultValue: 'false'
                            },
                            value: 'false'
                        }
                    }
                ],
                comment: '',
                dataType: 'VARCHAR',
                typeName: 'VARCHAR',
                size: 30,
                octectLength: 30,
                decimalDigits: 0,
                radix: 10,
                defaultValue: null,
                nullable: true,
                position: 2,
                markedDeleted: false,
                tableName: 'product_class',
                partOfCompositePrimaryKey: false,
                primaryKey: false
            },
            filteredByProfileAttribute: false,
            filteredByRoleVisibility: false,
            identifier: false,
            partOfCompositeIdentifier: false
        }
    ],
    calculatedBusinessColumns: [],
    relationships: [],
    simpleBusinessColumns: [],
    physicalTable: {
        physicalTableIndex: 0
    }
}

const mockedMeta = {
    physicalModels: [
        {
            name: 'product_class',
            properties: [
                {
                    'structural.deleted': {
                        propertyType: {
                            id: 'structural.deleted',
                            name: 'Deleted',
                            description: 'Specify if this column was deleted after a db update',
                            admissibleValues: [],
                            defaultValue: 'false'
                        },
                        value: 'false'
                    }
                }
            ],
            comment: '',
            type: 'TABLE',
            columns: [
                {
                    id: null,
                    name: 'product_class_id',
                    uniqueName: null,
                    description: null,
                    properties: [
                        {
                            'structural.deleted': {
                                propertyType: {
                                    id: 'structural.deleted',
                                    name: 'Deleted',
                                    description: 'Specify if this column was deleted after a db update',
                                    admissibleValues: [],
                                    defaultValue: 'false'
                                },
                                value: 'false'
                            }
                        }
                    ],
                    comment: '',
                    dataType: 'INTEGER',
                    typeName: 'INT',
                    size: 10,
                    octectLength: 0,
                    decimalDigits: 0,
                    radix: 10,
                    defaultValue: null,
                    nullable: false,
                    position: 1,
                    markedDeleted: false,
                    tableName: 'product_class',
                    partOfCompositePrimaryKey: false,
                    primaryKey: true
                },
                {
                    id: null,
                    name: 'product_subcategory',
                    uniqueName: null,
                    description: null,
                    properties: [
                        {
                            'structural.deleted': {
                                propertyType: {
                                    id: 'structural.deleted',
                                    name: 'Deleted',
                                    description: 'Specify if this column was deleted after a db update',
                                    admissibleValues: [],
                                    defaultValue: 'false'
                                },
                                value: 'false'
                            }
                        }
                    ],
                    comment: '',
                    dataType: 'VARCHAR',
                    typeName: 'VARCHAR',
                    size: 30,
                    octectLength: 30,
                    decimalDigits: 0,
                    radix: 10,
                    defaultValue: null,
                    nullable: true,
                    position: 2,
                    markedDeleted: false,
                    tableName: 'product_class',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                },
                {
                    id: null,
                    name: 'product_category',
                    uniqueName: null,
                    description: null,
                    properties: [
                        {
                            'structural.deleted': {
                                propertyType: {
                                    id: 'structural.deleted',
                                    name: 'Deleted',
                                    description: 'Specify if this column was deleted after a db update',
                                    admissibleValues: [],
                                    defaultValue: 'false'
                                },
                                value: 'false'
                            }
                        }
                    ],
                    comment: '',
                    dataType: 'VARCHAR',
                    typeName: 'VARCHAR',
                    size: 30,
                    octectLength: 30,
                    decimalDigits: 0,
                    radix: 10,
                    defaultValue: null,
                    nullable: true,
                    position: 3,
                    markedDeleted: false,
                    tableName: 'product_class',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                },
                {
                    id: null,
                    name: 'product_department',
                    uniqueName: null,
                    description: null,
                    properties: [
                        {
                            'structural.deleted': {
                                propertyType: {
                                    id: 'structural.deleted',
                                    name: 'Deleted',
                                    description: 'Specify if this column was deleted after a db update',
                                    admissibleValues: [],
                                    defaultValue: 'false'
                                },
                                value: 'false'
                            }
                        }
                    ],
                    comment: '',
                    dataType: 'VARCHAR',
                    typeName: 'VARCHAR',
                    size: 30,
                    octectLength: 30,
                    decimalDigits: 0,
                    radix: 10,
                    defaultValue: null,
                    nullable: true,
                    position: 4,
                    markedDeleted: false,
                    tableName: 'product_class',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                },
                {
                    id: null,
                    name: 'product_family',
                    uniqueName: null,
                    description: null,
                    properties: [
                        {
                            'structural.deleted': {
                                propertyType: {
                                    id: 'structural.deleted',
                                    name: 'Deleted',
                                    description: 'Specify if this column was deleted after a db update',
                                    admissibleValues: [],
                                    defaultValue: 'false'
                                },
                                value: 'false'
                            }
                        }
                    ],
                    comment: '',
                    dataType: 'VARCHAR',
                    typeName: 'VARCHAR',
                    size: 30,
                    octectLength: 30,
                    decimalDigits: 0,
                    radix: 10,
                    defaultValue: null,
                    nullable: true,
                    position: 5,
                    markedDeleted: false,
                    tableName: 'product_class',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                }
            ],
            primaryKey: {
                id: null,
                name: 'PRIMARY',
                uniqueName: null,
                description: null,
                properties: [],
                columns: [
                    {
                        id: null,
                        name: 'product_class_id',
                        uniqueName: null,
                        description: null,
                        properties: [
                            {
                                'structural.deleted': {
                                    propertyType: {
                                        id: 'structural.deleted',
                                        name: 'Deleted',
                                        description: 'Specify if this column was deleted after a db update',
                                        admissibleValues: [],
                                        defaultValue: 'false'
                                    },
                                    value: 'false'
                                }
                            }
                        ],
                        comment: '',
                        dataType: 'INTEGER',
                        typeName: 'INT',
                        size: 10,
                        octectLength: 0,
                        decimalDigits: 0,
                        radix: 10,
                        defaultValue: null,
                        nullable: false,
                        position: 1,
                        markedDeleted: false,
                        tableName: 'product_class',
                        partOfCompositePrimaryKey: false,
                        primaryKey: true
                    }
                ]
            },
            foreignKeys: []
        }
    ]
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(MetawebAttributesTab, {
        props: { selectedBusinessModel: mockedBusinessModel, propMeta: mockedMeta },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                Checkbox,
                Column,
                DataTable,
                InputText,
                MetawebAttributeDetailDialog: true,
                MetawebAttributeUnusedFieldDialog: true,
                'router-view': true
            },
            mocks: {
                $t: (msg) => msg,
                $confirm
            }
        }
    })
}

describe('Metaweb Attributes Tab', () => {
    it('the detail field of a business model attribute should open clicking the edit icon of the item', async () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual(mockedBusinessModel)
        expect(wrapper.html()).toContain('Product class id')
        expect(wrapper.html()).toContain('Product subcategory')

        await wrapper.find('[data-test="open-icon-Product class id"]').trigger('click')

        expect(wrapper.vm.attributeDetailDialogVisible).toBe(true)
        expect(wrapper.vm.selectedAttribute).toStrictEqual(mockedBusinessModel.columns[0])
    })

    it('the detail field of a business model should ask for confirm when deleting an attribute', async () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual(mockedBusinessModel)

        await wrapper.find('[data-test="delete-icon-Product class id"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)
    })

    it('the add attribute of a business model should show unused columns if some of them are missing from the physical model', async () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual(mockedBusinessModel)

        wrapper.find('[data-test="add-button"]').trigger('click')

        expect(wrapper.vm.unusedFields).toStrictEqual(mockedMeta.physicalModels[0].columns.slice(2))
    })
})
