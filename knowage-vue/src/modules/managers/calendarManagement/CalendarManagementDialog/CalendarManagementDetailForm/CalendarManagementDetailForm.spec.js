import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import CalendarManagementDetailForm from './CalendarManagementDetailForm.vue'
import Calendar from 'primevue/calendar'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedCalendar = {
    calendarId: 1,
    calendar: 'Test',
    calType: 'Generic',
    calStartDay: new Date(1498867200000),
    calEndDay: new Date(1500336000000),
    recStatus: 'A'
}

vi.mock('axios')

const $http = {
    post: vi.fn().mockImplementation(() => Promise.resolve({ data: [] }))
}

const $store = {
    state: {
        user: {
            functionalities: ['ManageCalendar']
        }
    },
    commit: jest.fn()
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(CalendarManagementDetailForm, {
        props: { propCalendar: mockedCalendar, generateButtonVisible: true, generateButtonDisabled: true },
        global: {
            plugins: [PrimeVue],
            stubs: { Button, Calendar, Dialog, InputText, CalendarManagementDetailForm: true, CalendarManagementDetailTable: true, ProgressBar, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $store,
                $http,
                $confirm
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Calendar Management Detail Form', () => {
    it('Should disable editing if calendar is readonly', async () => {
        const wrapper = factory()

        expect(wrapper.find('[data-test="calendar-name-input"]').wrapperElement._value).toBe('Test')
        expect(wrapper.find('[data-test="calendar-name-input"]').element.disabled).toBe(true)
        expect(wrapper.find('[data-test="calendar-type-input"]').wrapperElement._value).toBe('Generic')
        expect(wrapper.find('[data-test="calendar-type-input"]').element.disabled).toBe(true)
        expect(wrapper.find('[data-test="calendar-start-date-input"]').element.disabled).toBe(true)
        expect(wrapper.find('[data-test="calendar-end-date-input"]').element.disabled).toBe(true)
        expect(wrapper.vm.readonly).toBe(true)
    })
})
