import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import InputText from 'primevue/inputtext'
import DocumentBrowserTable from './DocumentBrowserTable.vue'

const mockedDocuments = [
    {
        id: 3249,
        dataSetId: null,
        name: 'Testlink - Registry simple',
        description: 'Simple registry displaying stores list. Used in TESTLINK test cases.',
        datasetsIds: null,
        label: 'TESTLINK-REGISTRY_SIMPLE',
        profiledVisibility: null,
        stateCode: 'REL',
        creationUser: 'demo_admin',
        refreshSeconds: null,
        objMetaDataAndContents: null,
        tenant: null,
        previewFile: null,
        docVersion: null,
        parametersRegion: 'east',
        lockedByUser: null,
        outputParameters: [],
        stateCodeStr: '',
        drivers: null,
        dataSetLabel: null,
        dataSourceLabel: null,
        metamodelDrivers: null,
        visible: true,
        typeCode: 'DATAMART',
        public: true,
        engine: 'knowageqbeengine',
        creationDate: '2019-08-14 09:32:40',
        functionalities: ['/Functionalities/Analytical Engines/Registry']
    },
    {
        id: 3250,
        dataSetId: null,
        name: 'Testlink - Registry pivot table',
        description: 'Registry displaying data as a pivot table. Used in TESTLINK test cases.',
        datasetsIds: null,
        label: 'TESTLINK-REGISTRY_PIVOT',
        profiledVisibility: null,
        stateCode: 'REL',
        creationUser: 'demo_admin',
        refreshSeconds: null,
        objMetaDataAndContents: null,
        tenant: null,
        previewFile: null,
        docVersion: null,
        parametersRegion: 'east',
        lockedByUser: null,
        outputParameters: [],
        stateCodeStr: 'Released',
        drivers: null,
        dataSetLabel: null,
        dataSourceLabel: null,
        metamodelDrivers: null,
        visible: true,
        typeCode: 'DATAMART',
        public: true,
        engine: 'knowageqbeengine',
        creationDate: '2019-09-19 10:25:43',
        functionalities: ['/Functionalities/Analytical Engines/Registry']
    },
    {
        id: 3251,
        dataSetId: null,
        name: 'Registry_Test_1',
        description: '',
        datasetsIds: null,
        label: 'Registry_Test_1',
        profiledVisibility: null,
        stateCode: 'DEV',
        creationUser: 'demo_admin',
        refreshSeconds: null,
        objMetaDataAndContents: null,
        tenant: null,
        previewFile: null,
        docVersion: null,
        parametersRegion: 'east',
        lockedByUser: null,
        outputParameters: [],
        stateCodeStr: 'Development',
        drivers: null,
        dataSetLabel: null,
        dataSourceLabel: null,
        metamodelDrivers: null,
        visible: true,
        typeCode: 'DATAMART',
        public: true,
        engine: 'knowageqbeengine',
        creationDate: '2019-06-11 07:09:05',
        functionalities: ['/Functionalities/Analytical Engines/Registry']
    }
]

const $store = {
    state: {
        user: { isSuperadmin: true, functionalities: ['DocumentManagement'] }
    }
}

const factory = (documents) => {
    return mount(DocumentBrowserTable, {
        props: {
            propDocuments: documents
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                Column,
                DataTable,
                InputText,
                Message
            },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

describe('Document Browser Table', () => {
    it('should shows an hint when loaded empty', () => {
        const wrapper = factory([])

        expect(wrapper.vm.documents.length).toBe(0)
        expect(wrapper.find('[data-test="no-documents-hint"]').exists()).toBe(true)
    })
    it('should show a list of documents in the detail if a folder is selected', async () => {
        const wrapper = factory(mockedDocuments)

        expect(wrapper.vm.documents.length).toBe(3)
        expect(wrapper.find('[data-test="documents-datatable"]').html()).toContain('Testlink - Registry simple')
        expect(wrapper.find('[data-test="documents-datatable"]').html()).toContain('TESTLINK-REGISTRY_PIVOT')
        expect(wrapper.find('[data-test="documents-datatable"]').html()).toContain('Registry_Test_1')
    })
    it('emits proper data when row is clicked', async () => {
        const wrapper = factory(mockedDocuments)

        expect(wrapper.vm.documents.length).toBe(3)

        await wrapper.find('[data-test="document-status"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('selected')
        expect(wrapper.emitted()['selected'][0][0]).toStrictEqual(mockedDocuments[0])
    })
})
