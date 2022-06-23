import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import axios from 'axios'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
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

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/listAllJobs`:
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

const factory = () => {
    return mount(WorkspaceSchedulationView, {
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
                InputText,
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

describe('Workspace Schedulation View', () => {
    it('should show an hint if no elements are present in the selected mode', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: {
                    root: []
                }
            })
        )
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.jobs).toStrictEqual([])

        expect(wrapper.find('[data-test="schedulation-table"]').html()).toContain('common.info.noDataFound')
    })

    it('should show a table with jobs', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.jobs).toStrictEqual(mockedJobs)

        expect(wrapper.find('[data-test="schedulation-table"]').html()).toContain('A - Bojan')
        expect(wrapper.find('[data-test="schedulation-table"]').html()).toContain('Mocked Job')
        expect(wrapper.find('[data-test="schedulation-table"]').html()).toContain('Development')
    })

    it('should filter the list of elements if a searchtext is provided', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.jobs).toStrictEqual(mockedJobs)

        await wrapper.find('[data-test="search-input"]').setValue('Mocked Job')

        expect(wrapper.find('[data-test="schedulation-table"]').html()).not.toContain('A - Bojan')
        expect(wrapper.find('[data-test="schedulation-table"]').html()).toContain('Mocked Job')
        expect(wrapper.find('[data-test="schedulation-table"]').html()).not.toContain('Development')

        await wrapper.find('[data-test="search-input"]').setValue('Development')

        expect(wrapper.find('[data-test="schedulation-table"]').html()).not.toContain('A - Bojan')
        expect(wrapper.find('[data-test="schedulation-table"]').html()).not.toContain('Mocked Job')
        expect(wrapper.find('[data-test="schedulation-table"]').html()).toContain('Development')
    })
})
