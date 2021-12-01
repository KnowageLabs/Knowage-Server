import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import axios from 'axios'
import Button from 'primevue/button'
import DocumentExecution from './DocumentExecution.vue'
import Menu from 'primevue/contextmenu'
import ProgressBar from 'primevue/progressbar'
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
    filterStatus: [],
    isReadyForExecution: true
}

const mockedURLData = {
    engineLabel: 'knowagecockpitengine',
    sbiExecutionId: 'a91c2cc6528011ecbb122d0218783d4c',
    url:
        '/knowagecockpitengine/api/1.0/pages/execute?user_id=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZGVtb19hZG1pbiIsImV4cCI6MTYzODM4MjQxNH0.s153N77bkgnsoXZuiJf7VipMarGg19P_SifndE5Xggw&SPAGOBI_AUDIT_ID=48965&DOCUMENT_LABEL=Document+Test&DOCUMENT_OUTPUT_PARAMETERS=%5B%5D&knowage_sys_country=US&DOCUMENT_COMMUNITIES=%5B%5D&manual_numero=5&DOCUMENT_IS_VISIBLE=true&SBI_EXECUTION_ROLE=%2Fdemo%2Fadmin&lista=%7B%3B%7BNon-Consumable%7DSTRING%7D&knowage_sys_language=en&manual_string=Food&DOCUMENT_FUNCTIONALITIES=%5B726%5D&SBI_COUNTRY=US&DOCUMENT_AUTHOR=demo_admin&manual_data=01%2F01%2F2002&document=3309&manual_numero_description=5&IS_TECHNICAL_USER=true&SBI_LANGUAGE=en&DOCUMENT_NAME=Copy+of+DOC_DEFAULT_2&manual_data_description=01%2F01%2F2002&NEW_SESSION=TRUE&manual_string_description=Food&DOCUMENT_IS_PUBLIC=true&lista_description=Non-Consumable&DOCUMENT_VERSION=8059&SBI_ENVIRONMENT=DOCBROWSER&SBI_EXECUTION_ID=a91c2cc6528011ecbb122d0218783d4c&IS_FOR_EXPORT=true&EDIT_MODE=null',
    typeCode: 'DOCUMENT_COMPOSITE'
}

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/Document`:
                return Promise.resolve({ data: mockedDocument })
            default:
                return Promise.resolve({ data: [] })
        }
    }),

    post: axios.post.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentexecution/filters`:
                return Promise.resolve({ data: mockedFilterData })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/url`:
                return Promise.resolve({ data: mockedURLData })
            default:
                return Promise.resolve({ data: [] })
        }
    })
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

const $route = { path: '/document-composite/Document%20Test' }

const factory = () => {
    return mount(DocumentExecution, {
        props: {
            id: 'Document'
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Button,
                DocumentExecutionHelpDialog: true,
                DocumentExecutionRankDialog: true,
                DocumentExecutionNotesDialog: true,
                DocumentExecutionMetadataDialog: true,
                DocumentExecutionMailDialog: true,
                DocumentExecutionSchedulationsTable: true,
                DocumentExecutionLinkDialog: true,
                Dossier: true,
                KnParameterSidebar: true,
                Menu,
                ProgressBar,
                Registry: true,
                Toolbar,
                'router-link': true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $http,
                $router,
                $route
            }
        }
    })
}

describe('Document Execution - Document has no parameters', () => {
    it('no parameters sidenav is shown', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.filtersData).toStrictEqual(mockedFilterData)
        expect(wrapper.vm.filtersData).toStrictEqual(mockedFilterData)
        expect(wrapper.vm.filtersData.filterStatus.length).toBe(0)

        expect(wrapper.find('[data-test="parameter-sidebar-icon"]').exists()).toBe(false)
        expect(wrapper.find('[data-test="parameter-sidebar-icon"]').exists()).toBe(false)
    })
})
