import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Chip from 'primevue/chip'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import KnParameterSidebar from './KnParameterSidebar.vue'
import Menu from 'primevue/contextmenu'
import MultiSelect from 'primevue/multiselect'
import ProgressBar from 'primevue/progressbar'
import RadioButton from 'primevue/radiobutton'
import Toolbar from 'primevue/toolbar'

const mockedDocument = {
    id: 3309,
    dataSetId: null,
    name: 'Copy of DOC_DEFAULT_2',
    description: '',
    datasetsIds: null,
    label: 'Document',
    profiledVisibility: null,
    stateCode: 'REL',
    creationUser: 'demo_admin',
    refreshSeconds: 0,
    objMetaDataAndContents: null,
    tenant: 'DEMO',
    previewFile: null,
    docVersion: null,
    parametersRegion: 'east',
    lockedByUser: 'false',
    outputParameters: [],
    stateCodeStr: 'sbidomains.nm.rel',
    drivers: [
        {
            id: 7637,
            parID: 653,
            parameter: {
                id: 653,
                label: '',
                name: '',
                description: '',
                type: 'STRING',
                typeId: null,
                length: null,
                mask: '',
                modality: '',
                modalityValue: null,
                modalityValueForDefault: null,
                modalityValueForMax: null,
                defaultFormula: '',
                valueSelection: null,
                selectedLayer: '',
                selectedLayerProp: '',
                checks: null,
                temporal: false,
                functional: false
            },
            label: 'manual_string',
            colSpan: null,
            thickPerc: null,
            prog: 1,
            priority: 4,
            parameterUrlName: 'manual_string',
            parameterValues: null,
            parameterValuesDescription: null,
            transientParmeters: false,
            hasValidValues: false,
            parameterValuesRetriever: null,
            maxValue: null,
            modifiable: 0,
            visible: true,
            biObjectID: 3309,
            iterative: false,
            required: true,
            multivalue: false
        },
        {
            id: 7635,
            parID: 651,
            parameter: {
                id: 651,
                label: '',
                name: '',
                description: '',
                type: 'NUM',
                typeId: null,
                length: null,
                mask: '',
                modality: '',
                modalityValue: null,
                modalityValueForDefault: null,
                modalityValueForMax: null,
                defaultFormula: '',
                valueSelection: null,
                selectedLayer: '',
                selectedLayerProp: '',
                checks: null,
                temporal: false,
                functional: false
            },
            label: 'manual_numero',
            colSpan: null,
            thickPerc: null,
            prog: 1,
            priority: 2,
            parameterUrlName: 'manual_numero',
            parameterValues: null,
            parameterValuesDescription: null,
            transientParmeters: false,
            hasValidValues: false,
            parameterValuesRetriever: null,
            maxValue: null,
            modifiable: 0,
            visible: true,
            biObjectID: 3309,
            iterative: false,
            required: true,
            multivalue: false
        },
        {
            id: 7636,
            parID: 652,
            parameter: {
                id: 652,
                label: '',
                name: '',
                description: '',
                type: 'DATE',
                typeId: null,
                length: null,
                mask: '',
                modality: '',
                modalityValue: null,
                modalityValueForDefault: null,
                modalityValueForMax: null,
                defaultFormula: '',
                valueSelection: null,
                selectedLayer: '',
                selectedLayerProp: '',
                checks: null,
                temporal: false,
                functional: false
            },
            label: 'manual_data',
            colSpan: null,
            thickPerc: null,
            prog: 1,
            priority: 3,
            parameterUrlName: 'manual_data',
            parameterValues: null,
            parameterValuesDescription: null,
            transientParmeters: false,
            hasValidValues: false,
            parameterValuesRetriever: null,
            maxValue: null,
            modifiable: 0,
            visible: true,
            biObjectID: 3309,
            iterative: false,
            required: true,
            multivalue: false
        },
        {
            id: 7632,
            parID: 655,
            parameter: {
                id: 655,
                label: '',
                name: '',
                description: '',
                type: 'STRING',
                typeId: null,
                length: null,
                mask: '',
                modality: '',
                modalityValue: null,
                modalityValueForDefault: null,
                modalityValueForMax: null,
                defaultFormula: '',
                valueSelection: null,
                selectedLayer: '',
                selectedLayerProp: '',
                checks: null,
                temporal: false,
                functional: false
            },
            label: 'Lista',
            colSpan: null,
            thickPerc: null,
            prog: 1,
            priority: 1,
            parameterUrlName: 'lista',
            parameterValues: null,
            parameterValuesDescription: null,
            transientParmeters: false,
            hasValidValues: false,
            parameterValuesRetriever: null,
            maxValue: null,
            modifiable: 0,
            visible: true,
            biObjectID: 3309,
            iterative: false,
            required: true,
            multivalue: true
        }
    ],
    dataSourceLabel: null,
    dataSetLabel: null,
    metamodelDrivers: null,
    visible: true,
    typeCode: 'DOCUMENT_COMPOSITE',
    public: true,
    creationDate: '2021-11-19 14:25:37',
    engine: 'knowagecockpitengine',
    functionalities: ['/Functionalities/Document Execution']
}

const mockedFilterData = {
    filterStatus: [
        {
            urlName: 'lista',
            metadata: {
                colsMap: {
                    _col1: 'PRODUCT_FAMILY_DESC',
                    _col0: 'PRODUCT_FAMILY'
                },
                descriptionColumn: 'PRODUCT_FAMILY_DESC',
                invisibleColumns: [],
                valueColumn: 'PRODUCT_FAMILY',
                visibleColumns: ['PRODUCT_FAMILY', 'PRODUCT_FAMILY_DESC']
            },
            visible: true,
            data: [
                {
                    value: 'Car',
                    description: '0'
                },
                {
                    value: 'Drink',
                    description: '0'
                },
                {
                    value: 'Food',
                    description: '0'
                },
                {
                    value: 'Non-Consumable',
                    description: '0'
                }
            ],
            valueSelection: 'lov',
            showOnPanel: 'true',
            driverUseLabel: 'A',
            label: 'Lista',
            driverDefaultValue: [
                {
                    _col0: 'Non-Consumable',
                    _col1: '0'
                }
            ],
            type: 'STRING',
            driverLabel: 'TEST_LINK_LIST_2',
            mandatory: true,
            allowInternalNodeSelection: false,
            multivalue: true,
            dependencies: {
                data: [],
                visual: [],
                lov: []
            },
            selectionType: 'LIST',
            id: 7632,
            parameterDescription: ['0'],
            parameterValue: [
                {
                    value: 'Non-Consumable',
                    description: '0'
                }
            ]
        },
        {
            urlName: 'manual_numero',
            metadata: {},
            visible: true,
            valueSelection: 'man_in',
            showOnPanel: 'true',
            driverUseLabel: 'A',
            label: 'manual_numero',
            driverDefaultValue: [
                {
                    value: '5',
                    desc: '5'
                }
            ],
            type: 'NUM',
            driverLabel: 'TEST_LINK_NUMBER',
            mandatory: true,
            allowInternalNodeSelection: false,
            multivalue: false,
            dependencies: {
                data: [],
                visual: [],
                lov: []
            },
            selectionType: '',
            id: 7635,
            parameterDescription: ['5'],
            parameterValue: [
                {
                    value: '5',
                    description: '5'
                }
            ]
        },
        {
            urlName: 'manual_data',
            metadata: {},
            visible: true,
            valueSelection: 'man_in',
            showOnPanel: 'true',
            driverUseLabel: 'A',
            label: 'manual_data',
            driverDefaultValue: [
                {
                    value: '01/01/2002',
                    desc: '2002-01-01#yyyy-mm-dd'
                }
            ],
            type: 'DATE',
            driverLabel: 'TEST_LINK_DATA',
            mandatory: true,
            allowInternalNodeSelection: false,
            multivalue: false,
            dependencies: {
                data: [],
                visual: [],
                lov: []
            },
            selectionType: '',
            id: 7636,
            parameterDescription: ['2002-01-01#yyyy-mm-dd'],
            parameterValue: [
                {
                    value: '01/01/2002',
                    description: '2002-01-01#yyyy-mm-dd'
                }
            ]
        },
        {
            urlName: 'manual_string',
            metadata: {},
            visible: true,
            valueSelection: 'man_in',
            showOnPanel: 'true',
            driverUseLabel: 'A',
            label: 'manual_string',
            driverDefaultValue: [
                {
                    value: 'Food',
                    desc: '0'
                }
            ],
            type: 'STRING',
            driverLabel: 'TEST_LINK_STRING',
            mandatory: true,
            allowInternalNodeSelection: false,
            multivalue: false,
            dependencies: {
                data: [],
                visual: [],
                lov: []
            },
            selectionType: '',
            id: 7637,
            parameterDescription: ['0'],
            parameterValue: [
                {
                    value: 'Food',
                    description: '0'
                }
            ]
        }
    ],
    isReadyForExecution: true
}

const $store = {
    state: {
        user: {
            sessionRole: '/demo/admin'
        }
    }
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(KnParameterSidebar, {
        props: {
            filtersData: mockedFilterData,
            propDocument: mockedDocument
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Button,
                Calendar,
                Chip,
                Checkbox,
                Dropdown,
                InputText,
                KnParameterPopupDialog: true,
                KnParameterTreeDialog: true,
                KnParameterSaveDialog: true,
                KnParameterSavedParametersDialog: true,
                Menu,
                MultiSelect,
                ProgressBar,
                RadioButton,
                Toolbar,
                'router-link': true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $router
            }
        }
    })
}

describe('Parameter Sidebar - Document has parameters', () => {
    it('should show an asterisk on required fields label', async () => {
        const wrapper = factory()

        expect(wrapper.vm.parameters.filterStatus[3].type).toBe('STRING')
        expect(wrapper.vm.parameters.filterStatus[3].mandatory).toBe(true)
        expect(wrapper.find('[data-test="parameter-input-label-7637"]').html()).toContain('*')

        expect(wrapper.vm.parameters.filterStatus[1].type).toBe('NUM')
        expect(wrapper.vm.parameters.filterStatus[1].mandatory).toBe(true)
        expect(wrapper.find('[data-test="parameter-input-label-7635"]').html()).toContain('*')

        expect(wrapper.vm.parameters.filterStatus[2].type).toBe('DATE')
        expect(wrapper.vm.parameters.filterStatus[2].mandatory).toBe(true)
        expect(wrapper.find('[data-test="parameter-date-label-7636"]').html()).toContain('*')

        expect(wrapper.vm.parameters.filterStatus[0].selectionType).toBe('LIST')
        expect(wrapper.vm.parameters.filterStatus[0].mandatory).toBe(true)
        expect(wrapper.find('[data-test="parameter-checkbox-label-7632"]').html()).toContain('*')
    })
    it('should show a parameter value if default value is set', async () => {
        const wrapper = factory()

        expect(wrapper.vm.parameters.filterStatus[3].type).toBe('STRING')
        expect(wrapper.vm.parameters.filterStatus[3].driverDefaultValue).toStrictEqual([{ value: 'Food', desc: '0' }])
        expect(wrapper.find('[data-test="parameter-input-7637"]').wrapperElement._value).toBe('Food')

        expect(wrapper.vm.parameters.filterStatus[1].type).toBe('NUM')
        expect(wrapper.vm.parameters.filterStatus[1].driverDefaultValue).toStrictEqual([{ value: '5', desc: '5' }])
        expect(wrapper.find('[data-test="parameter-input-7635"]').wrapperElement._value).toBe('5')

        expect(wrapper.vm.parameters.filterStatus[0].selectionType).toBe('LIST')
        expect(wrapper.vm.parameters.filterStatus[0].driverDefaultValue).toStrictEqual([{ _col0: 'Non-Consumable', _col1: '0' }])
        expect(wrapper.vm.selectedParameterCheckbox[7632]).toStrictEqual(['Non-Consumable'])
    })
    it('should show a clear button if a parameter value is present', async () => {
        const wrapper = factory()

        expect(wrapper.vm.parameters.filterStatus[3].type).toBe('STRING')
        expect(wrapper.vm.parameters.filterStatus[3].parameterValue).toStrictEqual([{ value: 'Food', description: '0' }])
        expect(wrapper.find('[data-test="parameter-input-clear-7637"]').exists()).toBe(true)

        expect(wrapper.vm.parameters.filterStatus[1].type).toBe('NUM')
        expect(wrapper.vm.parameters.filterStatus[1].parameterValue).toStrictEqual([{ value: '5', description: '5' }])
        expect(wrapper.find('[data-test="parameter-input-clear-7635"]').exists()).toBe(true)

        expect(wrapper.vm.parameters.filterStatus[2].type).toBe('DATE')
        expect(wrapper.vm.parameters.filterStatus[2].parameterValue).toStrictEqual([{ value: '01/01/2002', description: '2002-01-01#yyyy-mm-dd' }])
        expect(wrapper.find('[data-test="parameter-date-clear-7636"]').exists()).toBe(true)

        expect(wrapper.vm.parameters.filterStatus[0].selectionType).toBe('LIST')
        expect(wrapper.vm.parameters.filterStatus[0].parameterValue).toStrictEqual([{ value: 'Non-Consumable', description: '0' }])
        expect(wrapper.find('[data-test="parameter-checkbox-clear-7632"]').exists()).toBe(true)
    })
})
