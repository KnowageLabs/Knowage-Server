import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import RadioButton from 'primevue/radiobutton'
import SchedulerTimingOutputTimingTab from './SchedulerTimingOutputTimingTab.vue'
import Toolbar from 'primevue/toolbar'

const mockedTrigger = {
    jobName: 'Save as document',
    jobGroup: 'BIObjectExecutions',
    chrono: {
        type: 'single',
        parameter: {}
    },
    documents: null,
    startDateTiming: '2021-10-11T22:00:27.197Z',
    startTimeTiming: '2021-10-12T15:19:27.197Z',
    endDateTiming: null,
    endTimeTiming: null
}

const factory = () => {
    return mount(SchedulerTimingOutputTimingTab, {
        props: {
            propTrigger: mockedTrigger
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Calendar,
                Dropdown,
                InputText,
                KnCron: true,
                RadioButton,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Scheduler Timing Tab', () => {
    it('should change the detail changing the execution type', async () => {
        const wrapper = factory()

        expect(wrapper.vm.trigger).toStrictEqual(mockedTrigger)

        await wrapper.find('[data-test="trigger-type-button-scheduler"]').trigger('click')
        expect(wrapper.vm.triggerType).toBe('scheduler')
        expect(wrapper.find('[data-test="scheduler-trigger"]').exists()).toBe(true)

        await wrapper.find('[data-test="trigger-type-button-event"]').trigger('click')
        expect(wrapper.vm.triggerType).toBe('event')
        expect(wrapper.find('[data-test="scheduler-trigger"]').exists()).toBe(false)
        expect(wrapper.find('[data-test="event-trigger-endDate"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="event-trigger"]').exists()).toBe(true)

        await wrapper.find('[data-test="trigger-type-button-single"]').trigger('click')
        expect(wrapper.vm.triggerType).toBe('single')
        expect(wrapper.find('[data-test="event-trigger"]').exists()).toBe(false)
        expect(wrapper.find('[data-test="single-execution-trigger"]').exists()).toBe(true)
    })
})
