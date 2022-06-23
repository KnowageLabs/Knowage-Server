import { flushPromises, mount } from '@vue/test-utils'
import { nextTick } from 'vue-demi'
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
import WorkspaceRecentView from './WorkspaceRecentView.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'

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
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/recents`:
                return Promise.resolve({ data: mockedDocuments })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $router = {
    push: vi.fn()
}

const factory = (toggleCardDisplay) => {
    return mount(WorkspaceRecentView, {
        props: {
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
                WorkspaceCard,
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

describe('Workspace Recent View', () => {
    it('should show an hint if no elements are present in the selected mode', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.filteredDocuments.length).toBe(0)
        expect(wrapper.find('[data-test="recent-table"]').html()).toContain('common.info.noDataFound')
    })

    it('should show a table if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.filteredDocuments).toStrictEqual(mockedDocuments)
        expect(wrapper.vm.toggleCardDisplay).toBe(false)

        expect(wrapper.find('[data-test="recent-table"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="recent-table"]').html()).toContain('Mocked Document')
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

        expect(wrapper.vm.recentDocumentsList).toStrictEqual(mockedDocuments)
        expect(wrapper.vm.filteredDocuments).toStrictEqual(mockedDocuments)

        await wrapper.find('[data-test="search-input"]').setValue('CHOCOLATE')
        wrapper.vm.searchItems()

        vi.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="recent-table"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="recent-table"]').html()).not.toContain('Mocked Document')

        await wrapper.find('[data-test="search-input"]').setValue('Mocked')
        wrapper.vm.searchItems()

        vi.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="recent-table"]').html()).not.toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="recent-table"]').html()).toContain('Mocked Document')
    })

    it('should show a sidenav with details if one of the item is clicked', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.recentDocumentsList).toStrictEqual(mockedDocuments)
        expect(wrapper.vm.filteredDocuments).toStrictEqual(mockedDocuments)

        await wrapper.find('[data-test="info-button-Mocked Document"]').trigger('click')

        expect(wrapper.vm.showDetailSidebar).toBe(true)
        expect(wrapper.find('[data-test="detail-sidebar"]').exists()).toBe(true)
    })
})
