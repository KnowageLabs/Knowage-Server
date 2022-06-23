import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import CalendarManagementDialog from './CalendarManagementDialog.vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedCalendar = {
    realDateGenerated: [],
    splittedCalendar: [],
    calStartDay: null,
    calEndDay: null,
    calendar: '',
    calType: ''
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
    return mount(CalendarManagementDialog, {
        props: { visible: true, propCalendar: mockedCalendar, domains: [] },
        global: {
            plugins: [PrimeVue],
            stubs: { Button, Dialog, InputText, CalendarManagementDetailForm: true, CalendarManagementDetailTable: true, ProgressBar, Toolbar },
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
    vi.clearAllMocks()
})

describe('Calendar Management Dialog', () => {
    it('Should stop the saving or editing in case a mandatory field is empty', async () => {
        const wrapper = factory()

        expect(wrapper.vm.calendar.calendar).toBeFalsy()
        expect(wrapper.vm.calendar.calStartDay).toBeFalsy()
        expect(wrapper.vm.calendar.calEndDay).toBeFalsy()
        expect(wrapper.vm.buttonDisabled).toBe(true)
    })

    it('Should show a message if the saving is succesful', async () => {
        const wrapper = factory()

        wrapper.vm.calendar.name = 'Test'
        wrapper.vm.calendar.calStartDay = 1498867200000
        wrapper.vm.calendar.calEndDay = 1500336000000

        await wrapper.vm.saveCalendar()

        expect($http.post).toHaveBeenCalledTimes(1)
        expect($http.post).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'calendar/saveCalendar', { ...mockedCalendar, name: 'Test', calStartDay: 1498867200000, calEndDay: 1500336000000 })
        expect($store.commit).toHaveBeenCalledTimes(1)
    })

    it('hould inform the user that the generation may take some time', async () => {
        const wrapper = factory()

        wrapper.vm.calendar.name = 'Test'
        wrapper.vm.calendar.calStartDay = new Date(1498867200000)
        wrapper.vm.calendar.calEndDay = new Date(1500336000000)

        await wrapper.vm.generateCalendarConfirm()

        expect($confirm.require).toHaveBeenCalledTimes(1)
        expect($confirm.require).toHaveBeenCalledWith(expect.objectContaining({ message: 'managers.calendarManagement.generateConfirmMessage', header: 'managers.calendarManagement.generateConfirmTitle' }))
    })
})
