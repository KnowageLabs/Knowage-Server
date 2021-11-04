import { flushPromises, mount } from '@vue/test-utils'
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
import WorkspaceDataView from './WorkspaceDataView.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import { nextTick } from 'vue-demi'

const mockedDatasets = [
    {
        drivers: [],
        description: 'Dataset created from execution of document function_catalog by user demo_user',
        tags: [],
        dateIn: '2017-02-08T13:36:04Z',
        author: 'demo_user',
        name: 'demo_user_function_catalog_salesOutput',
        id: 1,
        owner: 'demo_user',
        label: 'demo_user_function_catalog_salesOutput',
        catTypeId: null,
        dsTypeCd: 'File',
        pars: []
    },
    {
        drivers: [],
        description: 'Dataset created from execution of document function_catalog by user demo_user',
        tags: [],
        dateIn: '2017-02-08T13:36:04Z',
        author: 'demo_user',
        name: 'Mocked Dataset',
        id: 2,
        owner: 'demo_user',
        label: 'Mocked Dataset',
        catTypeId: null,
        dsTypeCd: 'File',
        pars: []
    },
    {
        drivers: [],
        description: 'Dataset created from execution of document function_catalog by user demo_user',
        tags: [],
        dateIn: '2017-02-08T13:36:04Z',
        author: 'demo_user',
        name: 'Test Unit',
        id: 3,
        owner: 'demo_user',
        label: 'Test Unit',
        catTypeId: null,
        dsTypeCd: 'File',
        pars: []
    },
    {
        drivers: [],
        description: 'Dataset created from execution of document function_catalog by user demo_user',
        tags: [],
        dateIn: '2017-02-08T13:36:04Z',
        author: 'demo_user',
        name: 'Mocked',
        id: 4,
        owner: 'demo_user',
        label: 'Mocked',
        catTypeId: null,
        dsTypeCd: 'File',
        pars: []
    }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/owned/`:
                return Promise.resolve({ data: { root: mockedDatasets } })
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
    push: jest.fn()
}

const factory = (cardDisplay) => {
    return mount(WorkspaceDataView, {
        props: {
            toggleCardDisplay: cardDisplay
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
                WorkspaceAnalysisViewEditDialog: true,
                WorkspaceWarningDialog: true,
                WorkspaceAnalysisViewShareDialog: true,
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

describe('Workspace Analysis View', () => {
    it('should show an hint if no elements are present in the selected mode', async () => {
        axios.get.mockReturnValueOnce(
            Promise.resolve({
                data: { root: [] }
            })
        )
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.datasetList.length).toBe(0)
        expect(wrapper.vm.filteredDatasets.length).toBe(0)
        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('common.info.noDataFound')
    })

    xit('should show a table if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.analysisDocuments.length).toBe(4)
        expect(wrapper.vm.filteredAnalysisDocuments).toStrictEqual()

        expect(wrapper.vm.toggleCardDisplay).toBe(false)

        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Mocked')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Copy of CHOCOLATE_RATINGS(1)')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Admin')
    })

    xit('should show cards if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(true)

        await flushPromises()

        expect(wrapper.vm.analysisDocuments.length).toBe(4)
        expect(wrapper.vm.filteredAnalysisDocuments).toStrictEqual()

        expect(wrapper.vm.toggleCardDisplay).toBe(true)

        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Mocked')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Copy of CHOCOLATE_RATINGS(1)')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Admin')
    })

    xit('should filter the list of elements if a searchtext is provided', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.analysisDocuments.length).toBe(4)
        expect(wrapper.vm.filteredAnalysisDocuments).toStrictEqual()

        await wrapper.find('[data-test="search-input"]').setValue('CHOCOLATE')
        wrapper.vm.searchItems()

        jest.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="analysis-table"]').html()).not.toContain('Mocked')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Copy of CHOCOLATE_RATINGS(1)')

        await wrapper.find('[data-test="search-input"]').setValue('Mocked')
        wrapper.vm.searchItems()

        jest.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="analysis-table"]').html()).not.toContain('CHOCOLATE_RATINGS')
        expect(wrapper.find('[data-test="analysis-table"]').html()).not.toContain('Copy of CHOCOLATE_RATINGS(1)')
        expect(wrapper.find('[data-test="analysis-table"]').html()).toContain('Mocked')
    })

    xit('should show a sidenav with details if one of the item is clicked', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.analysisDocuments.length).toBe(4)
        expect(wrapper.vm.filteredAnalysisDocuments).toStrictEqual()

        await wrapper.find('[data-test="info-button-CHOCOLATE_RATINGS"]').trigger('click')

        expect(wrapper.vm.showDetailSidebar).toBe(true)
        expect(wrapper.find('[data-test="detail-sidebar"]').exists()).toBe(true)
    })
})
