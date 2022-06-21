import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import TimespanDetail from './TimespanDetail.vue'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedTimespans = [
    {
        name: 'Time Test',
        id: 81,
        type: 'time',
        definition: [
            {
                from: '08:50',
                to: '10:51'
            },
            {
                from: '10:52',
                to: '12:53'
            },
            {
                from: '12:54',
                to: '14:55'
            }
        ],
        category: '',
        staticFilter: false,
        commonInfo: 'it.eng.spagobi.commons.metadata.SbiCommonInfo@4e7e056a'
    },
    {
        name: 'Temporal Test',
        id: 82,
        type: 'temporal',
        definition: [
            {
                from: '04/03/2022',
                to: '05/03/2022',
                fromLocalized: '3/4/22',
                toLocalized: '3/5/22'
            },
            {
                from: '17/03/2022',
                to: '31/03/2022',
                fromLocalized: '17/03/2022',
                toLocalized: '31/03/2022'
            },
            {
                from: '01/04/2022',
                to: '14/04/2022',
                fromLocalized: '01/04/2022',
                toLocalized: '14/04/2022'
            }
        ],
        category: '',
        staticFilter: false,
        commonInfo: 'it.eng.spagobi.commons.metadata.SbiCommonInfo@3485798'
    }
]

jest.mock('axios')
const $http = {
    get: axios.get.mockImplementation(() => Promise.resolve({ data: mockedTimespans[0] })),
    post: axios.post.mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = (id) => {
    return mount(TimespanDetail, {
        props: {
            id: id,
            clone: 'false',
            categories: [],
            timespans: mockedTimespans
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                TimespanForm: true,
                TimespanIntervalTable: true,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Timespan Detail', () => {
    it('saves timespan', async () => {
        const wrapper = factory('81')

        await flushPromises()

        expect(wrapper.vm.timespan).toStrictEqual(mockedTimespans[0])

        wrapper.find("[data-test='save-button']").trigger('click')

        await flushPromises()

        expect(axios.post).toHaveBeenCalledTimes(1)
        expect(axios.post).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/timespan/saveTimespan`, mockedTimespans[0])
        expect($store.commit).toHaveBeenCalledTimes(1)
    })
})
