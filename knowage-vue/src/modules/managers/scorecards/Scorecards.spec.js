import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
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
import mainStore from '../../../App.store'

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

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpiee/listScorecard`:
                return Promise.resolve({ data: mockedScorecards })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $confirm = {
    require: vi.fn()
}

const $router = {
    push: vi.fn()
}

const factory = () => {
    return mount(Scorecards, {
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
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
                $confirm,
                $router
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Scorecards', () => {
    it('should show a loader when opened', async () => {
        factory()
        const store = mainStore()

        await flushPromises()
        expect(store.setLoading).toHaveBeenCalledTimes(2)
        expect(store.setLoading).toHaveBeenNthCalledWith(1, true)
        expect(store.setLoading).toHaveBeenNthCalledWith(2, false)
    })

    it('should show a list of scroecards', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.html()).toContain('Company Scorecard')
        expect(wrapper.html()).toContain('Retail Scorecard')
        expect(wrapper.html()).toContain('Test Vue')
    })
})
