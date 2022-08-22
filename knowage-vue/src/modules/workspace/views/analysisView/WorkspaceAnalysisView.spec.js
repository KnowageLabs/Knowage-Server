import { flushPromises, mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Menu from 'primevue/contextmenu'
import Message from 'primevue/message'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import WorkspaceAnalysisView from './WorkspaceAnalysisView.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import { nextTick } from 'vue-demi'

const mockedAnalysis = [
    {
        id: 3287,
        label: 'Admin',
        name: 'Admin',
        shortName: 'Admin',
        description: '',
        typeCode: 'KPI',
        typeId: 16,
        encrypt: 0,
        visible: 1,
        profiledVisibility: null,
        engine: 'Kpi Engine',
        engineid: 3,
        datasource: null,
        dataset: null,
        uuid: '3a8c2805-38c1-11ec-af14-0f32ebb46732',
        relname: null,
        stateCode: 'REL',
        stateId: 42,
        functionalities: [640],
        creationDate: '2021-10-29 16:05:10.0',
        creationUser: 'demo_admin',
        refreshSeconds: 0,
        parametersRegion: 'east',
        lockedByUser: 'false',
        pathResources: 'C:\\Users\\bojan.sovtic\\Desktop\\Setup\\apache-tomcat-8.5.37/resources\\DEMO',
        previewFile: 'test.png',
        isPublic: false,
        actions: [
            {
                name: 'delete',
                description: 'Delete this item'
            },
            {
                name: 'clone',
                description: 'Clone this item'
            },
            {
                name: 'showmetadata',
                description: 'Show Metadata'
            },
            {
                name: 'helpOnLine',
                description: 'Help OnLine'
            }
        ],
        exporters: ['PDF'],
        decorators: {
            isSavable: true
        }
    },
    {
        id: 3283,
        label: 'Mocked',
        name: 'Mocked',
        shortName: 'Mocked',
        description: 'Mocked',
        typeCode: 'DOCUMENT_COMPOSITE',
        typeId: 15,
        encrypt: 0,
        visible: 1,
        profiledVisibility: null,
        engine: 'Cockpit Engine',
        engineid: 22,
        datasource: null,
        dataset: null,
        uuid: '063ab34b-38bc-11ec-af14-0f32ebb46732',
        relname: null,
        stateCode: 'REL',
        stateId: 42,
        functionalities: [640],
        creationDate: '2021-10-29 15:27:55.0',
        creationUser: 'demo_user',
        refreshSeconds: 0,
        parametersRegion: 'east',
        lockedByUser: 'false',
        pathResources: 'C:\\Users\\bojan.sovtic\\Desktop\\Setup\\apache-tomcat-8.5.37/resources\\DEMO',
        previewFile: 'test.png',
        isPublic: false,
        actions: [
            {
                name: 'delete',
                description: 'Delete this item'
            },
            {
                name: 'clone',
                description: 'Clone this item'
            },
            {
                name: 'showmetadata',
                description: 'Show Metadata'
            },
            {
                name: 'helpOnLine',
                description: 'Help OnLine'
            }
        ],
        exporters: ['PDF'],
        decorators: {
            isSavable: true
        }
    },
    {
        id: 3224,
        label: 'CHOCOLATE_RATINGS',
        name: 'CHOCOLATE_RATINGS',
        shortName: 'CHOCOLATE_RATINGS',
        description: '',
        typeCode: 'DOCUMENT_COMPOSITE',
        typeId: 15,
        encrypt: 0,
        visible: 1,
        profiledVisibility: null,
        engine: 'Cockpit Engine',
        engineid: 22,
        datasource: null,
        dataset: null,
        uuid: '41c56902-d12d-11e8-b841-5d93a621484e',
        relname: null,
        stateCode: 'REL',
        stateId: 42,
        functionalities: [640],
        creationDate: '2020-01-20 19:09:12.0',
        creationUser: 'demo_user',
        refreshSeconds: 0,
        parametersRegion: 'east',
        lockedByUser: 'false',
        pathResources: 'C:\\Users\\bojan.sovtic\\Desktop\\Setup\\apache-tomcat-8.5.37/resources\\DEMO',
        previewFile: 'istockphoto-825383494-612x612.jpg',
        isPublic: false,
        actions: [
            {
                name: 'delete',
                description: 'Delete this item'
            },
            {
                name: 'clone',
                description: 'Clone this item'
            },
            {
                name: 'showmetadata',
                description: 'Show Metadata'
            },
            {
                name: 'helpOnLine',
                description: 'Help OnLine'
            }
        ],
        exporters: ['PDF'],
        decorators: {
            isSavable: true
        }
    },
    {
        id: 3284,
        label: 'Copy of CHOCOLATE_RATINGS(1)',
        name: 'Copy of CHOCOLATE_RATINGS(1)',
        shortName: 'Copy of CHOCOLATE_RATINGS(1)',
        description: '',
        typeCode: 'DOCUMENT_COMPOSITE',
        typeId: 15,
        encrypt: 0,
        visible: 1,
        profiledVisibility: null,
        engine: 'Cockpit Engine',
        engineid: 22,
        datasource: null,
        dataset: null,
        uuid: '097f08dc-38bc-11ec-af14-0f32ebb46732',
        relname: null,
        stateCode: 'REL',
        stateId: 42,
        functionalities: [613, 640, 611, 610],
        creationDate: '2021-10-29 15:28:01.0',
        creationUser: 'demo_user',
        refreshSeconds: 0,
        parametersRegion: 'east',
        lockedByUser: 'false',
        pathResources: 'C:\\Users\\bojan.sovtic\\Desktop\\Setup\\apache-tomcat-8.5.37/resources\\DEMO',
        previewFile: 'test.png',
        isPublic: true,
        actions: [
            {
                name: 'delete',
                description: 'Delete this item'
            },
            {
                name: 'clone',
                description: 'Clone this item'
            },
            {
                name: 'showmetadata',
                description: 'Show Metadata'
            },
            {
                name: 'helpOnLine',
                description: 'Help OnLine'
            }
        ],
        exporters: ['PDF'],
        decorators: {
            isSavable: true
        }
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `documents/myAnalysisDocsList`:
                return Promise.resolve({ data: { root: mockedAnalysis } })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $router = {
    push: vi.fn()
}

const factory = (cardDisplay) => {
    return mount(WorkspaceAnalysisView, {
        props: {
            toggleCardDisplay: cardDisplay
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue, createTestingPinia()],
            stubs: {
                Button,
                DetailSidebar: true,
                InputText,
                Column,
                DataTable,
                KnFabButton,
                Menu,
                Message,
                KnInputFile: true,
                ProgressBar,
                Toolbar,
                WorkspaceAnalysisViewEditDialog: true,
                WorkspaceWarningDialog: true,
                WorkspaceAnalysisViewShareDialog: true,
                WorkspaceCard,
                WorkspaceCockpitDialog: true,
                'router-link': true
            },
            mocks: {
                $t: (msg) => msg,

                $http,
                $router
            }
        }
    })
}

vi.useFakeTimers()
vi.spyOn(global, 'setTimeout')

describe('Workspace Analysis View', () => {
    it('should show an hint if no elements are present in the selected mode', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: { root: [] }
            })
        )
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.analysisDocuments.length).toBe(0)
        expect(wrapper.vm.filteredAnalysisDocuments.length).toBe(0)
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('common.info.noDataFound')
    })

    it('should show a table if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.analysisDocuments.length).toBe(4)
        expect(wrapper.vm.filteredAnalysisDocuments).toStrictEqual(mockedAnalysis)

        expect(wrapper.vm.toggleCardDisplay).toBe(false)

        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Mocked')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Copy of CHOCOLATE_RATINGS(1)')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Admin')
    })

    it('should show cards if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(true)

        await flushPromises()

        expect(wrapper.vm.analysisDocuments.length).toBe(4)
        expect(wrapper.vm.filteredAnalysisDocuments).toStrictEqual(mockedAnalysis)

        expect(wrapper.vm.toggleCardDisplay).toBe(true)

        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Mocked')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Copy of CHOCOLATE_RATINGS(1)')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Admin')
    })

    it('should filter the list of elements if a searchtext is provided', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.analysisDocuments.length).toBe(4)
        expect(wrapper.vm.filteredAnalysisDocuments).toStrictEqual(mockedAnalysis)

        await wrapper.find('[data-test="search-input"]').setValue('CHOCOLATE')
        expect(wrapper.find('[data-test="search-input"]').element.value).toBe('CHOCOLATE')
        wrapper.vm.searchWord = 'CHOCOLATE'
        wrapper.vm.searchItems()

        vi.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="analysis-table"]').html()).not.toContain('Mocked')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Copy of CHOCOLATE_RATINGS(1)')

        await wrapper.find('[data-test="search-input"]').setValue('Mocked')
        expect(wrapper.find('[data-test="search-input"]').element.value).toBe('Mocked')
        wrapper.vm.searchWord = 'Mocked'
        wrapper.vm.searchItems()

        vi.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="analysis-table"]').html()).not.toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="analysis-table"]').html()).not.toContain('Copy of CHOCOLATE_RATINGS(1)')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Mocked')
    })

    it('should show a sidenav with details if one of the item is clicked', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.analysisDocuments.length).toBe(4)
        expect(wrapper.vm.filteredAnalysisDocuments).toStrictEqual(mockedAnalysis)

        await wrapper.find('[data-test="info-button-CHOCOLATE_RATINGS"]').trigger('click')

        expect(wrapper.find('[data-test="detail-sidebar"]').exists()).toBe(true)
    })
})
