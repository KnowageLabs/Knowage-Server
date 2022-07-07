import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Scorecards from './Scorecards.vue'
import Toolbar from 'primevue/toolbar'

const mockedScorecards = [
    {
        id: 64,
        name: 'Company Scorecard',
        creationDate: 1477324102000,
        author: 'demo_admin',
        perspectives: []
    },
    {
        id: 70,
        name: 'Retail Scorecard',
        creationDate: 1486634111000,
        author: 'demo_admin',
        perspectives: []
    },
    {
        id: 114,
        name: 'Test Vue',
        creationDate: 1652092210000,
        author: 'demo_admin',
        perspectives: []
    }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpiee/listScorecard`:
                return Promise.resolve({ data: mockedScorecards })
            default:
                return Promise.resolve({ data: [] })
        }
    })
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

const factory = () => {
    return mount(Scorecards, {
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                Column,
                DataTable,
                InputText,
                KnFabButton,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $http,
                $store,
                $confirm,
                $router
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Scorecards', () => {
    it('should show a loader when opened', async () => {
        factory()

        await flushPromises()
        expect($store.commit).toHaveBeenCalledTimes(2)
        expect($store.commit).toHaveBeenNthCalledWith(1, 'setLoading', true)
        expect($store.commit).toHaveBeenNthCalledWith(2, 'setLoading', false)
    })

    it('should show a list of scroecards', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.html()).toContain('Company Scorecard')
        expect(wrapper.html()).toContain('Retail Scorecard')
        expect(wrapper.html()).toContain('Test Vue')
    })
})
