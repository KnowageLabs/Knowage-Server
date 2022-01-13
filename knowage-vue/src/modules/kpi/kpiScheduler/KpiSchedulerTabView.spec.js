import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputNumber from 'primevue/inputnumber'
import flushPromises from 'flush-promises'
import PrimeVue from 'primevue/config'
import RadioButton from 'primevue/radiobutton'
import Listbox from 'primevue/listbox'
import KpiSchedulerTabView from './KpiSchedulerTabView.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedScheduler = {
    id: 1,
    name: 'Test',
    kpis: [],
    kpiNames: null,
    author: 'demo_admin',
    jobStatus: 'ACTIVE',
    frequency: {
        cron: '{"type":"minute","parameter":{"numRepetition":"4"}}',
        startDate: 1626786082000,
        endDate: 1526786082000,
        startTime: '15:1',
        endTime: null
    }
}

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/1/loadSchedulerKPI':
                return Promise.resolve({ data: mockedScheduler })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/KPI_PLACEHOLDER_TYPE`:
                return Promise.resolve({ data: [] })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/KPI_PLACEHOLDER_FUNC`:
                return Promise.resolve({ data: [] })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all/':
                return Promise.resolve({ data: [] })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listKpi':
                return Promise.resolve({ data: [] })
        }
    }),
    post: axios.post.mockImplementation(() => Promise.resolve({ data: [] }))
}

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(KpiSchedulerTabView, {
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Button,
                Card,
                Column,
                DataTable,
                InputNumber,
                Listbox,
                KpiSchedulerKpiCard: true,
                KpiSchedulerFiltersCard: true,
                Cron: true,
                KpiSchedulerExecuteCard: true,
                ProgressBar,
                RadioButton,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('KPI Scheduler loading', () => {
    it('disables the save button if one required input is empty', async () => {
        const wrapper = factory()

        await flushPromises()

        wrapper.vm.validCron = false

        expect(wrapper.vm.selectedSchedule.kpis.length).toBe(0)
        expect(wrapper.vm.validCron).toBe(false)
        expect(wrapper.vm.buttonDisabled).toBe(true)
        expect(wrapper.find('[data-test="submit-button"]').element.disabled).toBe(true)
    })
    it('disables filter tab if no filters are present', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(Object.keys(wrapper.vm.formatedFilters).length).toBe(0)
        expect(wrapper.find('.p-tabview-panel:nth-child(2)').isVisible()).toBe(false)
    })
})
