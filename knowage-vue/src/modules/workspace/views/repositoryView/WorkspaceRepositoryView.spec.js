import { flushPromises, mount } from '@vue/test-utils'
import { nextTick } from 'vue-demi'
import PrimeVue from 'primevue/config'
import axios from 'axios'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Menu from 'primevue/contextmenu'
import Message from 'primevue/message'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import WorkspaceRepositoryView from './WorkspaceRepositoryView.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'

const mockedFolders = [
    {
        functId: 41,
        parentFunct: null,
        code: 'demo_user',
        name: 'Home',
        descr: 'demo_user',
        path: '/demo_user',
        prog: 1,
        timeIn: 1477316531000,
        userIn: 'demo_user'
    },
    {
        functId: 44,
        parentFunct: 41,
        code: 'To be checked',
        name: 'To be checked',
        descr: '',
        path: '/demo_user/To%20be%20checked',
        prog: 1,
        timeIn: 1486633333000,
        userIn: 'demo_user'
    },
    {
        functId: 50,
        parentFunct: 41,
        code: 'Test One',
        name: 'Test One',
        descr: '',
        path: '/demo_user/Test%20One',
        prog: 1,
        timeIn: 1635765986000,
        userIn: 'demo_user'
    },
    {
        functId: 58,
        parentFunct: 50,
        code: 'Bla',
        name: 'Bla',
        descr: '',
        path: '/demo_user/Test%20One/Bla',
        prog: 1,
        timeIn: 1635777380000,
        userIn: 'demo_user'
    },
    {
        functId: 61,
        parentFunct: 41,
        code: 'TEsfds',
        name: 'dsfsf',
        descr: null,
        path: '/demo_user/TEsfds',
        prog: 1,
        timeIn: 1635778875000,
        userIn: 'demo_user'
    },
    {
        functId: 63,
        parentFunct: 41,
        code: 'Test',
        name: 'Test',
        descr: null,
        path: '/demo_user/Test',
        prog: 1,
        timeIn: 1635779294000,
        userIn: 'demo_user'
    },
    {
        functId: 65,
        parentFunct: 44,
        code: 'Bojasfsaf',
        name: 'Bojasfsaf',
        descr: null,
        path: '/demo_user/To%20be%20checked/Bojasfsaf',
        prog: 1,
        timeIn: 1635779567000,
        userIn: 'demo_user'
    }
]

const mockedDocuments = [
    {
        objId: null,
        documentLabel: 'CHOCOLATE_RATINGS',
        documentName: 'CHOCOLATE_RATINGS',
        documentDescription: '',
        documentType: 'DOCUMENT_COMPOSITE',
        subObjId: null,
        subObjName: null,
        parameters: null,
        engineName: null,
        previewFile: 'istockphoto-825383494-612x612.jpg',
        requestTime: 0,
        functId: 41,
        biObjId: 3293,
        documentPath: '/demo_user'
    },
    {
        objId: null,
        documentLabel: 'Mocked Document',
        documentName: 'Mocked Document',
        documentDescription: '',
        documentType: 'DOCUMENT_COMPOSITE',
        subObjId: null,
        subObjName: null,
        parameters: null,
        engineName: null,
        previewFile: 'istockphoto-825383494-612x612.jpg',
        requestTime: 0,
        functId: 41,
        biObjId: 3293,
        documentPath: '/demo_user'
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/organizer/folders/`:
                return Promise.resolve({ data: mockedFolders })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/organizer/documents/1`:
                return Promise.resolve({ data: mockedDocuments })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $store = {
    state: {
        user: {}
    }
}

const $router = {
    push: vi.fn()
}

const factory = (toggleCardDisplay) => {
    return mount(WorkspaceRepositoryView, {
        props: {
            id: '1',
            toggleCardDisplay: toggleCardDisplay
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
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
                WorkspaceWarningDialog: true,
                WorkspaceRepositoryMoveDialog: true,
                WorkspaceCard,
                'router-link': true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $http,
                $router
            }
        }
    })
}

jest.useFakeTimers()
jest.spyOn(global, 'setTimeout')

describe('Workspace Repository View', () => {
    it('should show an hint if no elements are present in the selected mode', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.filteredDocuments.length).toBe(0)
        expect(wrapper.find('[data-test="documents-table"]').html()).toContain('common.info.noDataFound')
    })

    it('should show a table if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.filteredDocuments).toStrictEqual(mockedDocuments)
        expect(wrapper.vm.toggleCardDisplay).toBe(false)

        expect(wrapper.find('[data-test="documents-table"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="documents-table"]').html()).toContain('Mocked Document')
    })

    it('should show cards if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(true)

        await flushPromises()

        expect(wrapper.vm.filteredDocuments).toStrictEqual(mockedDocuments)
        expect(wrapper.vm.toggleCardDisplay).toBe(true)

        expect(wrapper.find('[data-test="card-container"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Mocked Document')
    })

    it('should filter the list of elements if a searchtext is provided', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.documents).toStrictEqual(mockedDocuments)
        expect(wrapper.vm.filteredDocuments).toStrictEqual(mockedDocuments)

        await wrapper.find('[data-test="search-input"]').setValue('CHOCOLATE')
        wrapper.vm.searchItems()

        jest.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="documents-table"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="documents-table"]').html()).not.toContain('Mocked Document')

        await wrapper.find('[data-test="search-input"]').setValue('Mocked')
        wrapper.vm.searchItems()

        jest.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="documents-table"]').html()).not.toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="documents-table"]').html()).toContain('Mocked Document')
    })

    it('should show a sidenav with details if one of the item is clicked', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.documents).toStrictEqual(mockedDocuments)
        expect(wrapper.vm.filteredDocuments).toStrictEqual(mockedDocuments)

        await wrapper.find('[data-test="info-button-Mocked Document"]').trigger('click')

        expect(wrapper.vm.showDetailSidebar).toBe(true)
        expect(wrapper.find('[data-test="detail-sidebar"]').exists()).toBe(true)
    })
})
