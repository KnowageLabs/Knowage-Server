import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import CalendarManagement from './CalendarManagement.vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedCalendars = [
    {
        calendarId: 5,
        calendar: 'nuovo cal',
        calType: 'nuovo',
        calStartDay: 1498867200000,
        calEndDay: 1500336000000,
        recStatus: 'A'
    },
    {
        calendarId: 7,
        calendar: 'testcase_KNOWAGE-1743:',
        calType: 'Generic',
        calStartDay: 1483228800000,
        calEndDay: 1514678400000,
        recStatus: 'A'
    },
    {
        calendarId: 22,
        calendar: 'Test',
        calType: 'Type',
        calStartDay: 1647993600000,
        calEndDay: 1648166400000,
        recStatus: 'A'
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `calendar/getCalendarList`:
                return Promise.resolve({ data: mockedCalendars })
            default:
                return Promise.resolve({ data: [] })
        }
    }),
    post: vi.fn().mockImplementation(() => Promise.resolve({ data: [] }))
}

const $confirm = {
    require: vi.fn()
}
const $store = {
    state: {
        user: {
            functionalities: ['ManageCalendar']
        }
    }
}
const factory = () => {
    return mount(CalendarManagement, {
        global: {
            plugins: [PrimeVue],
            stubs: { Button, Column, DataTable, InputText, KnFabButton, KnOverlaySpinnerPanel, ProgressBar, Toolbar },
            mocks: {
                $t: (msg) => msg,

                $confirm,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Calendar Management loading', () => {
    it('show progress bar when loading', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="spinner"]').exists()).toBe(true)
    })

    it('Should filter the table if the search text is provided', async () => {
        const wrapper = factory()

        await flushPromises()

        const dataTable = wrapper.find('[data-test="calendar-table"]')
        const inputSearch = wrapper.find('[data-test="search-input"]')

        expect(dataTable.html()).toContain('nuovo cal')
        expect(dataTable.html()).toContain('testcase_KNOWAGE-1743')
        expect(dataTable.html()).toContain('Test')

        await inputSearch.setValue('nuovo cal')
        expect(dataTable.html()).toContain('nuovo cal')
        expect(dataTable.html()).not.toContain('testcase_KNOWAGE-1743')
        expect(dataTable.html()).not.toContain('Test')

        await inputSearch.setValue('Generic')
        expect(dataTable.html()).not.toContain('nuovo cal')
        expect(dataTable.html()).toContain('testcase_KNOWAGE-1743')
        expect(dataTable.html()).not.toContain('Test')
    })

    it('Should show a popup if an item is selected', async () => {
        const wrapper = factory()

        await flushPromises()

        await wrapper.find('[data-test="calendar-start-day-Test"]').trigger('click')

        expect(wrapper.vm.selectedCalendar).toStrictEqual({
            calendarId: 22,
            calendar: 'Test',
            calType: 'Type',
            calStartDay: 1647993600000,
            calEndDay: 1648166400000,
            recStatus: 'A'
        })
        expect(wrapper.vm.calendarDialogVisible).toBe(true)
    })

    it('Should delete an item if the delete button in clicked', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.calendars.length).toBe(3)

        await wrapper.find('[data-test="delete-button-Test"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        wrapper.vm.deleteCalendar(mockedCalendars[2])

        expect($http.post).toHaveBeenCalledTimes(1)
        expect($http.post).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'calendar/22/deleteCalendar')
    })
})
