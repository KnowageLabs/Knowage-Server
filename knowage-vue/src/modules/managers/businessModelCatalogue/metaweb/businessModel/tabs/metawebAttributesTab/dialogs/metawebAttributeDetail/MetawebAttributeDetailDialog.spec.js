import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import MetawebAttributeDetailDialog from './MetawebAttributeDetailDialog.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedAttribute = {
    id: null,
    name: 'Promotion id',
    uniqueName: 'promotion_id',
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
                value: 'promotion'
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
        name: 'promotion_id',
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
        tableName: 'promotion',
        partOfCompositePrimaryKey: false,
        primaryKey: true
    },
    filteredByProfileAttribute: false,
    filteredByRoleVisibility: false,
    identifier: true,
    partOfCompositeIdentifier: false
}

const factory = () => {
    return mount(MetawebAttributeDetailDialog, {
        props: { selectedAttribute: mockedAttribute },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                Dialog,
                Dropdown,
                InputText,
                MultiSelect,
                ProgressBar,
                Toolbar,
                'router-view': true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Metaweb Attributes Detail Dialog', () => {
    it('the detail field of a business model should show the name of the selected field', async () => {
        const wrapper = factory()

        expect(wrapper.vm.attribute).toStrictEqual(mockedAttribute)
        expect(wrapper.vm.attribute.name).toStrictEqual('Promotion id')
    })
})
