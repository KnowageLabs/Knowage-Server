import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Column from 'primevue/column'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import SchedulationAgendaDisplay from './SchedulationAgendaDisplay.vue'
import ProgressBar from 'primevue/progressbar'

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
    post: vi.fn().mockImplementation(() => Promise.resolve())
}

const $router = {
    replace: vi.fn()
}

const factory = () => {
    return mount(SchedulationAgendaDisplay, {
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Column,
                Card,
                DataTable,
                KnValidationMessages,
                InputText,
                ProgressBar
            },
            mocks: {
                $t: (msg) => msg,
                $router,
                $http
            }
        },
        propsData: {
            id: `10`
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Scheduler Agenda', () => {
    it('should not change the number of item if a different view type is set', async () => {
        const wrapper = factory()
        await wrapper.setData({ dataItemList: mockedResultList.root })

        var timeViewDisplayCount = 0
        var packageViewDisplayCount = 0
        var documenViewtDisplayCount = 0

        await wrapper.setData({ selectedDisplayMode: { name: 'time', code: 'time' } })
        timeViewDisplayCount = wrapper.vm.dataItemList.length

        await wrapper.setData({ selectedDisplayMode: { name: 'package', code: 'package' } })
        packageViewDisplayCount = wrapper.vm.dataItemList.length

        await wrapper.setData({ selectedDisplayMode: { name: 'document', code: 'document' } })
        documenViewtDisplayCount = wrapper.vm.dataItemList.length

        expect(timeViewDisplayCount).toStrictEqual(packageViewDisplayCount)
        expect(packageViewDisplayCount).toStrictEqual(documenViewtDisplayCount)
    })
})
