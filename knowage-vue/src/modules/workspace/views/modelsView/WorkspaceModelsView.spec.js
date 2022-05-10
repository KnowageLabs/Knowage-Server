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
import SelectButton from 'primevue/selectbutton'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import WorkspaceModelsView from './WorkspaceModelsView.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import WorkspaceModelsTable from './tables/WorkspaceModelsTable.vue'

const mockedBusinessModels = [
    {
        id: 140,
        name: 'Sales',
        description: 'Sales cube',
        category: 280,
        dataSourceLabel: 'Foodmart',
        dataSourceId: 1,
        modelLocked: true,
        modelLocker: null,
        smartView: true,
        tablePrefixLike: null,
        tablePrefixNotLike: null,
        drivers: [],
        metamodelDrivers: null
    },
    {
        id: 142,
        name: 'Inventory',
        description: 'Inventory cube',
        category: 168,
        dataSourceLabel: 'Foodmart',
        dataSourceId: 1,
        modelLocked: true,
        modelLocker: 'bsovtic',
        smartView: true,
        tablePrefixLike: null,
        tablePrefixNotLike: null,
        drivers: [],
        metamodelDrivers: null
    }
]

const mockedFederatedDataset = [
    {
        federation_id: 135,
        name: 'Test name',
        label: 'Test Label',
        description: 'Test Description',
        relationships:
            '[[{"bidirectional":true,"cardinality":"many-to-one","sourceTable":{"name":"EDS_sales","className":"EDS_sales"},"sourceColumns":["the_month"],"destinationTable":{"name":"demo_admin_function_catalog_dfR","className":"demo_admin_function_catalog_dfR"},"destinationColumns":["c2"]}]]',
        degenerated: false,
        owner: 'demo_admin'
    },
    {
        federation_id: 145,
        name: 'Bojan test',
        label: 'Bojan test',
        description: 'Bojan test',
        relationships: '[[{"bidirectional":true,"cardinality":"many-to-one","sourceTable":{"name":"EDS_sales","className":"EDS_sales"},"sourceColumns":["the_month"],"destinationTable":{"name":"EDS_Inventory","className":"EDS_Inventory"},"destinationColumns":["the_year"]}]]',
        degenerated: false,
        owner: 'demo_admin'
    }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/?fileExtension=jar`:
                return Promise.resolve({ data: mockedBusinessModels })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `federateddataset/`:
                return Promise.resolve({ data: mockedFederatedDataset })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $store = {
    state: {
        user: {
            functionalities: ['EnableFederatedDataset']
        }
    }
}

const $router = {
    push: jest.fn()
}

const factory = (cardDisplay) => {
    return mount(WorkspaceModelsView, {
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
                SelectButton,
                Toolbar,
                WorkspaceWarningDialog: true,
                WorkspaceCard,
                WorkspaceModelsTable,
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

describe('Workspace Models View', () => {
    it('should show an hint if no elements are present in the selected mode', async () => {
        for (let i = 0; i < 3; i++) {
            axios.get.mockReturnValueOnce(
                Promise.resolve({
                    data: []
                })
            )
        }

        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.businessModels).toStrictEqual([])
        expect(wrapper.vm.federatedDatasets).toStrictEqual([])
        expect(wrapper.vm.allItems).toStrictEqual([])

        expect(wrapper.find('[data-test="models-table"]').html()).toContain('common.info.noDataFound')
    })
    it('should show a table if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.businessModels.length).toBe(2)
        expect(wrapper.vm.federatedDatasets.length).toBe(2)

        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Sales')
        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Inventory')
        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Test name')
        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Bojan test')
    })

    it('should show cards if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(true)

        await flushPromises()

        expect(wrapper.vm.businessModels.length).toBe(2)
        expect(wrapper.vm.federatedDatasets.length).toBe(2)

        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Sales')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Inventory')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Test name')
        expect(wrapper.find('[data-test="card-container"]').html()).toContain('Bojan test')
    })

    it('should filter the list of elements if a searchtext is provided', async () => {
        const wrapper = factory(false)

        wrapper.vm.tableMode = 'Business'

        await flushPromises()

        expect(wrapper.vm.allItems.length).toBe(4)
        expect(wrapper.vm.filteredItems.length).toBe(4)

        await wrapper.find('[data-test="search-input"]').setValue('Sales')
        wrapper.vm.searchItems()

        jest.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Sales')
        expect(wrapper.find('[data-test="models-table"]').html()).not.toContain('Inventory')
        expect(wrapper.find('[data-test="models-table"]').html()).not.toContain('Test name')

        await wrapper.find('[data-test="search-input"]').setValue('Test name')
        wrapper.vm.searchItems()

        jest.runAllTimers()
        await nextTick()

        expect(wrapper.find('[data-test="models-table"]').html()).not.toContain('Sales')
        expect(wrapper.find('[data-test="models-table"]').html()).not.toContain('Inventory')
        expect(wrapper.find('[data-test="models-table"]').html()).not.toContain('Test name')
    })

    it('should show a sidenav with details if one of the item is clicked', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.allItems.length).toBe(4)
        expect(wrapper.vm.filteredItems.length).toBe(4)

        await wrapper.find('[data-test="info-button-Sales"]').trigger('click')

        expect(wrapper.vm.showDetailSidebar).toBe(true)
        expect(wrapper.find('[data-test="detail-sidebar"]').exists()).toBe(true)
    })
})
