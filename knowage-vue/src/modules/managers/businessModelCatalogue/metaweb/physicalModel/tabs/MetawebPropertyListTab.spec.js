import { mount } from '@vue/test-utils'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import MetawebPropertyListTab from './MetawebPropertyListTab.vue'

const mockedPhysicalModel = {
    name: 'account',
    description: 'description',
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
    comment: 'COMMENT',
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

const factory = () => {
    return mount(MetawebPropertyListTab, {
        props: {
            selectedPhysicalModel: mockedPhysicalModel
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Accordion,
                AccordionTab,
                Button,
                InputText,
                'router-view': true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Metaweb Property List Tab', () => {
    it('clicking on a physical model in the list the input field should be read only', async () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedPhysicalModel).toStrictEqual(mockedPhysicalModel)

        expect(wrapper.find('[data-test="input-common.name"]').wrapperElement._value).toBe('account')
        expect(wrapper.find('[data-test="input-common.name"]').element.disabled).toBe(true)
        expect(wrapper.find('[data-test="input-common.description"]').wrapperElement._value).toBe('description')
        expect(wrapper.find('[data-test="input-common.description"]').element.disabled).toBe(true)
        expect(wrapper.find('[data-test="input-common.comment"]').wrapperElement._value).toBe('COMMENT')
        expect(wrapper.find('[data-test="input-common.comment"]').element.disabled).toBe(true)
        expect(wrapper.find('[data-test="input-Deleted"]').wrapperElement._value).toBe('false')
        expect(wrapper.find('[data-test="input-Deleted"]').element.disabled).toBe(true)
    })
})
