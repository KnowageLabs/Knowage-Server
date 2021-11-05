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

const mockedEnterprise = [
    {
        drivers: [],
        description: 'Dataset created from execution of document function_catalog by user demo_user',
        tags: [],
        dateIn: '2017-02-08T13:36:04Z',
        author: 'demo_user',
        name: 'Enterprise',
        id: 4,
        owner: 'demo_user',
        label: 'Enterprise',
        catTypeId: null,
        dsTypeCd: 'File',
        pars: []
    }
]

const mockedShared = [
    {
        drivers: [],
        description: 'Dataset created from execution of document function_catalog by user demo_user',
        tags: [],
        dateIn: '2017-02-08T13:36:04Z',
        author: 'demo_user',
        name: 'Shared',
        id: 4,
        owner: 'demo_user',
        label: 'Shared',
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
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/shared/`:
                return Promise.resolve({ data: { root: mockedShared } })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/enterprise/`:
                return Promise.resolve({ data: { root: mockedEnterprise } })
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

    it('should show dataset types if dataset is selected', async () => {
        const wrapper = factory(false)

        await flushPromises()

        await wrapper.find('[aria-label="Shared"]').trigger('click')

        expect(wrapper.vm.tableMode).toBe('Shared')
        expect(wrapper.vm.filteredDatasets).toStrictEqual(mockedShared)
        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('Shared')

        await wrapper.find('[aria-label="Enterprise"]').trigger('click')

        expect(wrapper.vm.tableMode).toBe('Enterprise')
        expect(wrapper.vm.filteredDatasets).toStrictEqual(mockedEnterprise)
        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('Enterprise')
    })

    it('should show a table if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.datasetList.length).toBe(4)
        expect(wrapper.vm.filteredDatasets).toStrictEqual(mockedDatasets)

        expect(wrapper.vm.toggleCardDisplay).toBe(false)

        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('emo_user_function_catalog_salesOutput')
        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('Mocked Dataset')
        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('Test Unit')
        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('Mocked')
    })

    it('should show cards if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(true)

        await flushPromises()

        expect(wrapper.vm.datasetList.length).toBe(4)
        expect(wrapper.vm.filteredDatasets).toStrictEqual(mockedDatasets)

        expect(wrapper.vm.toggleCardDisplay).toBe(true)

        expect(wrapper.find('[data-test="card-container"]').html()).toContain('emo_user_function_catalog_salesOutput')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Mocked Dataset')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Test Unit')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Mocked')
    })

    it('should filter the list of elements if a searchtext is provided', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.datasetList.length).toBe(4)
        expect(wrapper.vm.filteredDatasets).toStrictEqual(mockedDatasets)

        await wrapper.find('[data-test="search-input"]').setValue('Mocked')
        wrapper.vm.searchItems()

        jest.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('Mocked Dataset')
        expect(wrapper.find('[data-test="datasets-table"]').html()).not.toContain('Test Unit')
        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('Mocked')

        await wrapper.find('[data-test="search-input"]').setValue('Test')
        wrapper.vm.searchItems()

        jest.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="datasets-table"]').html()).not.toContain('Mocked Dataset')
        expect(wrapper.find('[data-test="datasets-table"]').html()).toContain('Test Unit')
        expect(wrapper.find('[data-test="datasets-table"]').html()).not.toContain('Mocked')
    })

    it('should show a sidenav with details if one of the item is clicked', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.datasetList.length).toBe(4)
        expect(wrapper.vm.filteredDatasets).toStrictEqual(mockedDatasets)

        await wrapper.find('[data-test="info-button-Mocked Dataset"]').trigger('click')

        expect(wrapper.vm.showDetailSidebar).toBe(true)
        expect(wrapper.find('[data-test="detail-sidebar"]').exists()).toBe(true)
    })
})
