import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'
import Button from 'primevue/button'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import Timespan from './Timespan.vue'
import TimespanHint from './TimespanHint.vue'
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

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/timespan/listDynTimespan`:
                return Promise.resolve({ data: mockedTimespans })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $confirm = {
    require: vi.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: TimespanHint
        },
        {
            path: '/timespan',
            component: TimespanHint
        },
        {
            path: '/timespan/new-timespan',
            component: null
        },
        {
            path: '/timespan/edit-timespan',
            props: (route) => ({ id: route.query.id, clone: route.query.clone }),
            component: null
        }
    ]
})

const factory = () => {
    return mount(Timespan, {
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [router],
            stubs: {
                Button,
                FabButton,
                KnListBox: true,
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

describe('Timespan loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })

    it('loads and shows timespans in a list', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.timespans).toStrictEqual(
            mockedTimespans.map((timespan) => {
                return { ...timespan, isCloneable: timespan.type === 'temporal' }
            })
        )
    })
})
