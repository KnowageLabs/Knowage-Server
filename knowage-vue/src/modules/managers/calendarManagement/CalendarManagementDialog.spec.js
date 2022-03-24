import { mount } from '@vue/test-utils'
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

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `calendar/getCalendarList`:
                return Promise.resolve({ data: mockedCalendars })
            default:
                return Promise.resolve({ data: [] })
        }
    }),
    post: axios.post.mockImplementation(() => Promise.resolve({ data: [] }))
}

const $confirm = {
    require: jest.fn()
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
                $store,
                $confirm,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Domains Management loading', () => {
    it('show progress bar when loading', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="spinner"]').exists()).toBe(true)
    })
})
