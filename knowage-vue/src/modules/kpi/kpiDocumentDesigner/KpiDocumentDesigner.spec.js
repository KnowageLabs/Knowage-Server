import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import KpiDocumentDesigner from './KpiDocumentDesigner.vue'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedKpi = []

const mockedScorecards = []

const mockedKpiTemplate = {
    chart: {
        type: 'kpi',
        model: 'list',
        data: {
            kpi: [
                { isSuffix: 'true', name: 'MARKUP', prefixSuffixValue: 'SE', rangeMaxValue: '14', rangeMinValue: '2', vieweas: 'Speedometer', category: 'PROFIT' },
                { isSuffix: 'false', name: 'ROTATION', prefixSuffixValue: 'SA', rangeMaxValue: '23', rangeMinValue: '3', vieweas: 'Speedometer', category: 'PROFIT' }
            ]
        },
        style: { font: { color: { r: 51, g: 31, b: 97 }, fontFamily: 'tahoma', fontWeight: 'bold', size: '1.3rem' } },
        options: { showtarget: true, showtargetpercentage: true, showthreshold: false, showvalue: false, vieweas: '', history: { size: '23', units: 'quarter' } }
    }
}

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/timespan/listDynTimespan`:
                return Promise.resolve({ data: mockedKpi })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpiee/listScorecard`:
                return Promise.resolve({ data: mockedScorecards })
            default:
                return Promise.resolve({ data: [] })
        }
    }),
    post: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_KPI_ENGINE_API_URL + `1.0/kpisTemplate/getKpiTemplate`:
                return Promise.resolve({ data: mockedKpiTemplate })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $confirm = {
    require: vi.fn()
}

const $store = {
    state: {
        user: {
            functionalities: ['ScorecardsManagement']
        }
    }
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(KpiDocumentDesigner, {
        props: {
            id: '1'
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                KpiDocumentDesignerDocumentTypeCard: true,
                KpiDocumentDesignerKpiListCard: true,
                KpiDocumentDesignerOptionsCard: true,
                KpiDocumentDesignerStyleCard: true,
                KpiDocumentDesignerTypeCard: true,
                KpiDocumentDesignerSaveDialog: true,
                KpiDocumentDesignerScorecardsListCard: true,
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
    vi.clearAllMocks()
})

describe('Kpi Edit loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})
