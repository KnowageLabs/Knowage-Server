import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'
import PrimeVue from 'primevue/config'
import Card from 'primevue/card'
import Toolbar from 'primevue/toolbar'
import ProgressBar from 'primevue/progressbar'
import Calendar from 'primevue/calendar'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import SchedulationAgenda from './SchedulationAgenda.vue'
import SchedulationAgendaHint from './SchedulationAgendaHint.vue'
import KnHint from '@/components/UI/KnHint.vue'
import moment from 'moment'
import flushPromises from 'flush-promises'

const mockedDocumentList = [
    {
        id: 3219,
        dataSetId: null,
        name: 'NYC Traffic Accidents',
        description: ''
    },
    {
        id: 2329,
        dataSetId: null,
        name: 'BestProductSingPar',
        description: 'BestProduct single pameter - Composite Document'
    },
    {
        id: 3224,
        dataSetId: null,
        name: 'CHOCOLATE_RATINGS',
        description: ''
    }
]

const mockedPackageList = {
    root: [
        {
            name: 'TEST_1',
            group: 'BIObjectExecutions',
            description: ''
        },
        {
            name: 'TEST_2',
            group: 'BIObjectExecutions',
            description: ''
        },
        {
            name: 'TEST_3',
            group: 'BIObjectExecutions',
            description: 'TEST_3'
        }
    ]
}
const mockedResultFiveDays = {
    root: [
        {
            name: 'TEST_1',
            group: 'BIObjectExecutions',
            description: '',
            triggers: [
                {
                    jobName: 'TEST_1',
                    jobGroup: 'BIObjectExecutions',
                    triggerName: 'test 123',
                    triggerGroup: 'DEFAULT',
                    triggerDescription: '',
                    triggerCalendarName: 'test 123',
                    triggerStartDate: '27/09/2021',
                    triggerStartTime: '11:17',
                    triggerEndDate: '26/10/2021',
                    triggerEndTime: '11:17',
                    triggerZonedStartTime: '2021-09-27T11:17:00.000+02:00',
                    triggerZonedEndTime: '2021-10-26T11:17:00.000+02:00',
                    triggerChronString: "{'type':'day','parameter':{'numRepetition':1}}",
                    triggerChronType: 'Scheduler-day',
                    triggerIsPaused: false,
                    start: '2021-09-27T11:17:00.000+02:00',
                    end: '2021-10-26T11:17:00.000+02:00',
                    documents: ['DEMO_Report'],
                    limited: false,
                    executions: ['2021-10-06T11:17:00.000+02:00', '2021-10-07T11:17:00.000+02:00', '2021-10-08T11:17:00.000+02:00', '2021-10-09T11:17:00.000+02:00', '2021-10-10T11:17:00.000+02:00']
                }
            ]
        }
    ]
}

const mockedResultList = {
    root: [
        {
            name: 'TEST_1',
            group: 'BIObjectExecutions',
            description: '',
            triggers: [
                {
                    jobName: 'TEST_1',
                    jobGroup: 'BIObjectExecutions',
                    triggerName: 'test 123',
                    triggerGroup: 'DEFAULT',
                    triggerDescription: '',
                    triggerCalendarName: 'test 123',
                    triggerStartDate: '27/09/2021',
                    triggerStartTime: '11:17',
                    triggerEndDate: '26/10/2021',
                    triggerEndTime: '11:17',
                    triggerZonedStartTime: '2021-09-27T11:17:00.000+02:00',
                    triggerZonedEndTime: '2021-10-26T11:17:00.000+02:00',
                    triggerChronString: "{'type':'day','parameter':{'numRepetition':1}}",
                    triggerChronType: 'Scheduler-day',
                    triggerIsPaused: false,

                    start: '2021-09-27T11:17:00.000+02:00',
                    end: '2021-10-26T11:17:00.000+02:00',
                    documents: ['DEMO_Report'],
                    limited: false,
                    executions: [
                        '2021-10-06T11:17:00.000+02:00',
                        '2021-10-07T11:17:00.000+02:00',
                        '2021-10-08T11:17:00.000+02:00',
                        '2021-10-09T11:17:00.000+02:00',
                        '2021-10-10T11:17:00.000+02:00',
                        '2021-10-11T11:17:00.000+02:00',
                        '2021-10-12T11:17:00.000+02:00',
                        '2021-10-13T11:17:00.000+02:00',
                        '2021-10-14T11:17:00.000+02:00',
                        '2021-10-15T11:17:00.000+02:00',
                        '2021-10-16T11:17:00.000+02:00',
                        '2021-10-17T11:17:00.000+02:00',
                        '2021-10-18T11:17:00.000+02:00',
                        '2021-10-19T11:17:00.000+02:00',
                        '2021-10-20T11:17:00.000+02:00',
                        '2021-10-21T11:17:00.000+02:00',
                        '2021-10-22T11:17:00.000+02:00',
                        '2021-10-23T11:17:00.000+02:00',
                        '2021-10-24T11:17:00.000+02:00',
                        '2021-10-25T11:17:00.000+02:00'
                    ]
                }
            ]
        },
        {
            name: 'TEST_2',
            group: 'BIObjectExecutions',
            description: 'TEST_2',
            triggers: [
                {
                    jobName: 'TEST_2',
                    jobGroup: 'BIObjectExecutions',
                    triggerName: 'TEST_2_S',
                    triggerGroup: 'DEFAULT',
                    triggerDescription: 'TEST_2_S',
                    triggerCalendarName: 'TEST_2_S',
                    triggerStartDate: '05/10/2021',
                    triggerStartTime: '21:37',
                    triggerEndDate: '05/11/2021',
                    triggerEndTime: '21:37',
                    triggerZonedStartTime: '2021-10-05T21:37:00.000+02:00',
                    triggerZonedEndTime: '2021-11-05T21:37:00.000+01:00',
                    triggerChronString: "{'type':'day','parameter':{'numRepetition':'20'}}",
                    triggerChronType: 'Scheduler-day',
                    triggerIsPaused: false,
                    start: '2021-10-05T21:37:00.000+02:00',
                    end: '2021-11-05T21:37:00.000+01:00',
                    documents: ['DEMO_Report'],
                    limited: false,
                    executions: ['2021-10-21T21:37:00.000+02:00', '2021-11-01T21:37:00.000+01:00']
                }
            ]
        },
        {
            name: 'TEST_3',
            group: 'BIObjectExecutions',
            description: 'TEST_3',
            triggers: [
                {
                    jobName: 'TEST_3',
                    jobGroup: 'BIObjectExecutions',
                    triggerName: 'TEST_3',
                    triggerGroup: 'DEFAULT',
                    triggerDescription: 'TEST_3',
                    triggerCalendarName: 'TEST_3',
                    triggerStartDate: '27/09/2021',
                    triggerStartTime: '21:43',
                    triggerEndDate: '28/10/2021',
                    triggerEndTime: '21:43',
                    triggerZonedStartTime: '2021-09-27T21:43:00.000+02:00',
                    triggerZonedEndTime: '2021-10-28T21:43:00.000+02:00',
                    triggerChronString: "{'type':'day','parameter':{'numRepetition':'20'}}",
                    triggerChronType: 'Scheduler-day',
                    triggerIsPaused: false,

                    start: '2021-09-27T21:43:00.000+02:00',
                    end: '2021-10-28T21:43:00.000+02:00',
                    documents: ['DEMO_Report', 'OLAP_SALES'],
                    limited: false,
                    executions: ['2021-10-21T21:43:00.000+02:00']
                }
            ]
        }
    ]
}

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `/2.0/documents`:
                return Promise.resolve({ data: mockedDocumentList })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `/scheduleree/listAllJobs`:
                return Promise.resolve({ data: mockedPackageList })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/nextExecutions?start=2021-11-06T00:11:00&end=2021-11-11T00:11:00`:
                return Promise.resolve({ data: mockedResultFiveDays })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/nextExecutions?start=2021-11-06T00:11:00&end=2025-11-11T00:11:00`:
                return Promise.resolve({ data: mockedResultList })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: SchedulationAgendaHint
        },
        {
            path: '/schedulation-agenda',
            component: SchedulationAgendaHint
        }
    ]
})

vi.mock('axios')

const $confirm = {
    require: jest.fn(() => {})
}

const $store = {
    commit: jest.fn(() => {})
}

const $router = {
    push: jest.fn(() => {})
}

const factory = () => {
    return mount(SchedulationAgenda, {
        global: {
            plugins: [PrimeVue, router],
            stubs: {
                Calendar,
                Card,
                Button,
                KnHint,
                InputText,
                Toolbar,
                ProgressBar,
                SchedulationAgendaHint,
                SchedulationAgendaDialog: true
            },
            mocks: {
                $t: (msg) => msg,

                $confirm,
                $router,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

beforeEach(() => {})

describe('Scheduler Agenda loading', () => {
    it('shows progress bar when loading', async () => {
        const wrapper = factory()
        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('should show an hint when loaded without a filter set', async () => {
        const wrapper = factory()
        await router.push('/schedulation-agenda')
        await router.isReady()
        await flushPromises()
        expect(wrapper.html()).toContain('managers.schedulationAgendaManagement.hint')
    })
})

describe('Scheduler Agenda', () => {
    it('should not be possible to select a starting date before the current moment', async () => {
        const wrapper = factory()
        await wrapper.setData({ startDate: new Date(2019, 10, 15) })
        expect(moment(wrapper.vm.startDate)).not.toBe('10/15/2019')
    })
    it('should return all the available packages from the next 5 days if launched without filter', async () => {
        const wrapper = factory()
        wrapper.vm.startDateTime = new Date(2021, 10, 6, 0, 0, 0)
        wrapper.vm.endDateTime = new Date(2021, 10, 11, 0, 0, 0)
        await wrapper.vm.runSearch()
        await flushPromises()
        expect(wrapper.vm.schedulations).toStrictEqual(mockedResultFiveDays.root)
    })
    it('should return all the available packaged from the days set if launched with filter', async () => {
        const wrapper = factory()
        wrapper.vm.startDateTime = new Date(2021, 10, 6, 0, 0, 0)
        wrapper.vm.endDateTime = new Date(2025, 10, 11, 0, 0, 0)
        await wrapper.vm.runSearch()
        await flushPromises()
        expect(wrapper.vm.schedulations).toStrictEqual(mockedResultList.root)
    })
})
