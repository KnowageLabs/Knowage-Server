import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import flushPromises from 'flush-promises'
import EventsManagement from './EventsManagement.vue'
import InputText from 'primevue/inputtext'
import moment from 'moment'
import PrimeVue from 'primevue/config'
import ProgressSpinner from 'primevue/progressspinner'
import Toolbar from 'primevue/toolbar'

const mockedEvents = [
    {
        id: 99171,
        user: 'Scheduler',
        date: 1652772900000,
        desc: '${scheduler.startexecsched} Mocked One',
        params: '',
        type: 'SCHEDULER',
        roles: ['/demo/admin', '/demo/user'],
        formattedDate: '17/05/2019 09:35:00',
        formattedDescription: 'Start scheduled execution of document Bojan',
        additionalInformation: {}
    },
    {
        id: 99170,
        user: 'Scheduler',
        date: 1049936295000,
        desc: '${scheduler.endexecsched} Mocked Two',
        params: '',
        type: 'SCHEDULER',
        roles: ['/demo/admin'],
        formattedDate: '14/04/2020 13:38:15',
        formattedDescription: 'End scheduled execution of document Bojan',
        additionalInformation: {}
    }
]

const filteredMockedEvents = [
    {
        id: 99171,
        user: 'Scheduler',
        date: 1652772900000,
        desc: '${scheduler.startexecsched} Mocked One',
        params: '',
        type: 'SCHEDULER',
        roles: ['/demo/admin', '/demo/user'],
        formattedDate: '17/05/2019 09:35:00',
        formattedDescription: 'Start scheduled execution of document Bojan',
        additionalInformation: {}
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/events/?fetchsize=20&offset=0&startDate=${encodeURIComponent(moment(new Date(2019, 10, 30)).format('YYYY-MM-DD+HH:mm:ss'))}&endDate=${encodeURIComponent(moment(new Date(2023, 10, 30)).format('YYYY-MM-DD+HH:mm:ss'))}`:
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/events/?fetchsize=20&offset=0&type=Mocked Event Model':
                return Promise.resolve({ data: { results: filteredMockedEvents, start: 1, total: 1 } })
            default:
                return Promise.resolve({ data: { results: mockedEvents, start: 1, total: 2 } })
        }
    })
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(EventsManagement, {
        attachToDocument: true,
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: { Button, Card, Calendar, Column, InputText, DataTable, Dropdown, ProgressSpinner, Toolbar },
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

describe('Events Management', () => {
    it('Should show a loader when opened', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-spinner"]').exists()).toBe(true)
    })

    it('Should filter the list if the date input is changed', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.html()).toContain('${scheduler.startexecsched} Mocked One')
        expect(wrapper.html()).toContain('${scheduler.endexecsched} Mocked Two')

        wrapper.vm.startDate = new Date(2019, 10, 30)
        wrapper.vm.endDate = new Date(2023, 10, 30)

        await wrapper.find('[data-test="search-button"]').trigger('click')
        await flushPromises()

        expect($http.get).toHaveBeenNthCalledWith(
            2,
            import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/events/?fetchsize=20&offset=0&startDate=${encodeURIComponent(moment(wrapper.vm.startDate).format('YYYY-MM-DD+HH:mm:ss'))}&endDate=${encodeURIComponent(moment(wrapper.vm.endDate).format('YYYY-MM-DD+HH:mm:ss'))}`
        )
        expect(wrapper.html()).toContain('${scheduler.startexecsched} Mocked One')
        expect(wrapper.html()).not.toContain('${scheduler.endexecsched} Mocked Two')
    })

    it('Should filter the list if the type input is changed', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.html()).toContain('${scheduler.startexecsched} Mocked One')
        expect(wrapper.html()).toContain('${scheduler.endexecsched} Mocked Two')

        wrapper.vm.selectedEventModel = 'Mocked Event Model'
        await wrapper.find('[data-test="search-button"]').trigger('click')
        await flushPromises()

        expect($http.get).toHaveBeenNthCalledWith(2, import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/events/?fetchsize=20&offset=0&type=Mocked Event Model`)
        expect(wrapper.html()).toContain('${scheduler.startexecsched} Mocked One')
        expect(wrapper.html()).not.toContain('${scheduler.endexecsched} Mocked Two')
    })
})
