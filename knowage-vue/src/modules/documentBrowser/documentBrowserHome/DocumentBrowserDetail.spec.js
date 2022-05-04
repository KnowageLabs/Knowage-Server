import { mount } from '@vue/test-utils'
import DocumentBrowserSidebar from './sidebar/DocumentBrowserSidebar.vue'
import DocumentBrowserDetail from './DocumentBrowserDetail.vue'
import Toolbar from 'primevue/toolbar'

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
        stateCodeStr: 'Released',
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
        user: {}
    }
}

const factory = (documents) => {
    return mount(DocumentBrowserDetail, {
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
                DocumentBrowserBreadcrumb: true,
                DocumentBrowserTable: true,
                DocumentBrowserSidebar,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

describe('Document Browser Detail', () => {
    it('should show detail sidenav if one of the row is selected', async () => {
        const wrapper = factory(mockedDocuments)

        expect(wrapper.vm.selectedDocument).toBe(null)

        expect(wrapper.find('[data-test="document-browser-sidebar"]').exists()).toBe(false)

        await wrapper.vm.setSelectedDocument(mockedDocuments[0])

        expect(wrapper.vm.selectedDocument).toStrictEqual(mockedDocuments[0])
        expect(wrapper.find('[data-test="document-browser-sidebar"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="document-browser-sidebar"]').html()).toContain('Testlink - Registry simple')
    })
})
