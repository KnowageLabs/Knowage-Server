import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { formatDate } from '@/helpers/commons/localeHelper'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import KpiSchedulerExecuteCard from './KpiSchedulerExecuteCard.vue'
import Toolbar from 'primevue/toolbar'

const mockedSchedule = {
    id: 1,
    name: 'PROFIT',
    delta: true,
    jobStatus: 'EXPIRED',
    kpiNames: 'CUSTOMER PARTICIPATION STD, RETENTION RATE, RETENTION RATE STD'
}

const mockedExecutionList = [
    {
        id: 542569,
        schedulerId: 75,
        timeRun: 1594037160000,
        output: '',
        errorCount: 0,
        successCount: 104,
        totalCount: 104,
        outputPresent: false
    },
    {
        id: 542558,
        schedulerId: 75,
        timeRun: 1594037100000,
        output: '',
        errorCount: 1,
        successCount: 0,
        totalCount: 1,
        outputPresent: true
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedExecutionList
        })
    )
}

const factory = () => {
    return mount(KpiSchedulerExecuteCard, {
        props: {
            selectedSchedule: mockedSchedule
        },
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Card,
                Column,
                DataTable,
                Dialog,
                InputText,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('KPI Scheduler Detail', () => {
    it('shows list of execution logs', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.schedule).toStrictEqual(mockedSchedule)
        expect(wrapper.vm.executionList.length).toBe(2)
        expect(wrapper.find('[data-test="executions-table"]').html()).toContain(formatDate(mockedExecutionList[0].timeRun, 'YYYY-MM-DD HH:mm:ss'))
        expect(wrapper.find('[data-test="executions-table"]').html()).toContain(formatDate(mockedExecutionList[1].timeRun, 'YYYY-MM-DD HH:mm:ss'))
        expect(wrapper.find('[data-test="executions-table"]').html()).toContain(104)
        expect(wrapper.find('[data-test="executions-table"]').html()).toContain(0)
    })
})
