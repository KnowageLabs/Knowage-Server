import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import axios from 'axios'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import MetawebPhysicalModel from './MetawebPhysicalModel.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Toolbar from 'primevue/toolbar'

const mockedPhysicalModel = {
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
}

vi.mock('axios')

const $http = { get: axios.get.mockImplementation(() => Promise.resolve({ data: [] })) }

const factory = () => {
    return mount(MetawebPhysicalModel, {
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                InputText,
                MetawebForeignKeyTab: true,
                MetawebPhysicalModelList: true,
                MetawebPropertyListTab: true,
                MetawebPhysicalModelUpdateDialog: true,
                TabView,
                TabPanel,
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

describe('Metaweb Physical Model', () => {
    it('clicking on a physical model in the list the detail should open in the detail section', async () => {
        const wrapper = factory()

        wrapper.vm.onSelectedItem(mockedPhysicalModel)

        await nextTick()

        expect(wrapper.vm.selectedPhysicalModel).toStrictEqual(mockedPhysicalModel)
        expect(wrapper.find(['[data-test="tab-view"]']).exists()).toBe(true)
        expect(wrapper.find('[data-test="physical-model-name"]').html()).toContain('account')
    })
})
