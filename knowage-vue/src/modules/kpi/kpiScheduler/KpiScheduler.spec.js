import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Chip from 'primevue/chip'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import Listbox from 'primevue/listbox'
import KpiScheduler from './KpiScheduler.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedSchedulers = [
    {
        id: 1,
        name: 'PROFIT',
        jobStatus: 'EXPIRED',
        kpiNames: 'CUSTOMER PARTICIPATION STD, RETENTION RATE, RETENTION RATE STD'
    },
    {
        id: 2,
        name: 'MARKUP',
        jobStatus: 'ACTIVE',
        kpiNames: 'YEARLY TURNOVER, % OF EMPLOYEE TRAINED'
    },
    {
        id: 3,
        name: 'ROTATION',
        jobStatus: 'SUSPENDED',
        kpiNames: 'OPERATING PROFIT MARGIN STD'
    }
]

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedSchedulers }))
axios.delete.mockImplementation(() => Promise.resolve())

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
    return mount(KpiScheduler, {
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Button,
                Card,
                Chip,
                FabButton,
                Listbox,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('KPI Scheduler loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows "no data" label when loaded empty', async () => {
        axios.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )

        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.schedulerList.length).toBe(0)
        expect(wrapper.html()).toContain('common.info.noDataFound')
    })
    it('the list shows kpi schedulers when loaded', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.schedulerList.length).toBe(3)
        expect(wrapper.html()).toContain('PROFIT')
        expect(wrapper.html()).toContain('MARKUP')
        expect(wrapper.html()).toContain('ROTATION')
        expect(wrapper.html()).toContain('YEARLY TURNOVER')
    })
})

describe('KPI Scheduler list', () => {
    it('shows a prompt when user click on a rule delete button to delete it and deletes it', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.schedulerList.length).toBe(3)

        await wrapper.find('[data-test="delete-button-1"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteSchedule(mockedSchedulers[0].id)
        expect(axios.delete).toHaveBeenCalledTimes(1)
        expect(axios.delete).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/2/deleteKpiScheduler')
        expect($store.commit).toHaveBeenCalledTimes(1)
    })
    it("changes url when the when the '+' button is clicked", async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="new-button"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/kpi-scheduler/new-kpi-schedule')
    })
    it('changes url with clicked row id when a row is clicked', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/kpi-scheduler/edit-kpi-schedule?id=2&clone=false')
    })
})
