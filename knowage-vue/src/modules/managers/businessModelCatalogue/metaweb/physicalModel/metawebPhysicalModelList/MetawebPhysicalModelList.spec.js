import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Button from 'primevue/button'
import Listbox from 'primevue/listbox'
import MetawebPhysicalModelList from './MetawebPhysicalModelList.vue'
import Toolbar from 'primevue/toolbar'

const metaMock = {
    physicalModels: [
        {
            name: 'account',
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
                    name: 'account_id',
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
                    tableName: 'account',
                    partOfCompositePrimaryKey: false,
                    primaryKey: true
                },
                {
                    id: null,
                    name: 'account_parent',
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
                    nullable: true,
                    position: 2,
                    markedDeleted: false,
                    tableName: 'account',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                },
                {
                    id: null,
                    name: 'account_description',
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
                    tableName: 'account',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                },
                {
                    id: null,
                    name: 'account_type',
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
                    nullable: false,
                    position: 4,
                    markedDeleted: false,
                    tableName: 'account',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                },
                {
                    id: null,
                    name: 'account_rollup',
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
                    nullable: false,
                    position: 5,
                    markedDeleted: false,
                    tableName: 'account',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                },
                {
                    id: null,
                    name: 'Custom_Members',
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
                    size: 255,
                    octectLength: 255,
                    decimalDigits: 0,
                    radix: 10,
                    defaultValue: null,
                    nullable: true,
                    position: 6,
                    markedDeleted: false,
                    tableName: 'account',
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
                        name: 'account_id',
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
                        tableName: 'account',
                        partOfCompositePrimaryKey: false,
                        primaryKey: true
                    }
                ]
            },
            foreignKeys: []
        },
        {
            name: 'category',
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
                    name: 'category_id',
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
                    nullable: false,
                    position: 1,
                    markedDeleted: false,
                    tableName: 'category',
                    partOfCompositePrimaryKey: false,
                    primaryKey: true
                },
                {
                    id: null,
                    name: 'category_parent',
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
                    tableName: 'category',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                },
                {
                    id: null,
                    name: 'category_description',
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
                    nullable: false,
                    position: 3,
                    markedDeleted: false,
                    tableName: 'category',
                    partOfCompositePrimaryKey: false,
                    primaryKey: false
                },
                {
                    id: null,
                    name: 'category_rollup',
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
                    tableName: 'category',
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
                        name: 'category_id',
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
                        nullable: false,
                        position: 1,
                        markedDeleted: false,
                        tableName: 'category',
                        partOfCompositePrimaryKey: false,
                        primaryKey: true
                    }
                ]
            },
            foreignKeys: []
        }
    ]
}

vi.mock('axios')

const $http = { get: vi.fn().mockImplementation(() => Promise.resolve({ data: [] })) }

const factory = () => {
    return mount(MetawebPhysicalModelList, {
        props: {
            propMeta: metaMock
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                Accordion,
                AccordionTab,
                Button,
                Listbox,
                Toolbar,
                'router-view': true
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

describe('Metaweb Physical Model List', () => {
    it('clicking on a physical model in the list the detail should open in the detail section', async () => {
        const wrapper = factory()

        expect(wrapper.vm.meta).toStrictEqual(metaMock)
        expect(wrapper.vm.meta.physicalModels.length).toBe(2)
        expect(wrapper.html()).toContain('account')
        expect(wrapper.html()).toContain('category')

        wrapper.find('[data-test="physical-model-tab-account"]').trigger('click')
        expect(wrapper.vm.selectedPhysicalModel).toStrictEqual(metaMock.physicalModels[0])
    })
})
