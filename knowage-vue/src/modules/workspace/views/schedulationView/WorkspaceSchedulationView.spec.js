import { flushPromises, mount } from '@vue/test-utils'
import { nextTick } from 'vue-demi'
import PrimeVue from 'primevue/config'
import axios from 'axios'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Menu from 'primevue/contextmenu'
import Message from 'primevue/message'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import WorkspaceSchedulationView from './WorkspaceSchedulationView.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import WorkspaceSchedulationTable from './tables/WorkspaceSchedulationTable.vue'

const mockedJobs = [
    {
        jobName: 'A - Bojan',
        jobGroup: 'BIObjectExecutions',
        jobDescription: '',
        jobClass: 'it.eng.spagobi.tools.scheduler.jobs.ExecuteBIDocumentJob',
        jobParameters: [],
        documents: [],
        triggers: []
    },
    {
        jobName: 'Mocked Job',
        jobGroup: 'BIObjectExecutions',
        jobDescription: '',
        jobClass: 'it.eng.spagobi.tools.scheduler.jobs.ExecuteBIDocumentJob',
        jobParameters: [],
        documents: [],
        triggers: []
    },
    {
        jobName: 'Development',
        jobGroup: 'BIObjectExecutions',
        jobDescription: '',
        jobClass: 'it.eng.spagobi.tools.scheduler.jobs.ExecuteBIDocumentJob',
        jobParameters: [],
        documents: [],
        triggers: []
    }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/listAllJobs`:
                return Promise.resolve({ data: { root: mockedJobs } })
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
    return mount(WorkspaceSchedulationView, {
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
                Column,
                DataTable,
                Menu,
                Message,
                KnInputFile: true,
                ProgressBar,
                Toolbar,
                WorkspaceWarningDialog: true,
                WorkspaceCard,
                WorkspaceSchedulationTable,
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

describe('Workspace Schedulation View', () => {
    it('should show an hint if no elements are present in the selected mode', async () => {
        axios.get.mockReturnValueOnce(
            Promise.resolve({
                data: {
                    root: []
                }
            })
        )
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.jobs).toStrictEqual([])

        expect(wrapper.find('[data-test="schedulation-table"]').html()).toContain('common.info.noDataFound')
    })
    xit('should show a table if grid mode is switched for the selected mode', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.businessModels.length).toBe(2)
        expect(wrapper.vm.federatedDatasets.length).toBe(2)

        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Sales')
        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Inventory')
        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Test name')
        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Bojan test')
    })

    xit('should filter the list of elements if a searchtext is provided', async () => {
        const wrapper = factory(false)

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
        expect(wrapper.find('[data-test="models-table"]').html()).toContain('Test name')
    })

    xit('should show a sidenav with details if one of the item is clicked', async () => {
        const wrapper = factory(false)

        await flushPromises()

        expect(wrapper.vm.allItems.length).toBe(4)
        expect(wrapper.vm.filteredItems.length).toBe(4)

        await wrapper.find('[data-test="info-button-Sales"]').trigger('click')

        expect(wrapper.vm.showDetailSidebar).toBe(true)
        expect(wrapper.find('[data-test="detail-sidebar"]').exists()).toBe(true)
    })
})
