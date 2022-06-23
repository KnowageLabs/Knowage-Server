import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { createRouter, createWebHistory } from 'vue-router'
import Button from 'primevue/button'
import Card from 'primevue/card'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import Scheduler from './Scheduler.vue'
import SchedulerHint from './SchedulerHint.vue'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedJobs = [
    {
        jobName: 'A - With Params',
        jobGroup: 'BIObjectExecutions',
        jobDescription: '',
        jobClass: 'it.eng.spagobi.tools.scheduler.jobs.ExecuteBIDocumentJob',
        jobDurability: true,
        jobRequestRecovery: false,
        jobMergeAllSnapshots: false,
        jobCollateSnapshots: false,
        useVolatility: false,
        jobParameters: [
            {
                name: 'documentLabels',
                value: 'DEMO_Report__1'
            },
            {
                name: 'DEMO_Report__1',
                value: 'age_range=30-40; 40-50; 60-70; 50-60 %26par_brand_name=%26prod_category=%26product_hierarchy=%26'
            },
            {
                name: 'empty',
                value: 'empty'
            }
        ],
        documents: [],
        numberOfDocuments: 0,
        triggers: []
    },
    {
        jobName: 'Test Parameters',
        jobGroup: 'BIObjectExecutions',
        jobDescription: 'dfsfdsdfssdfsdf',
        jobClass: 'it.eng.spagobi.tools.scheduler.jobs.ExecuteBIDocumentJob',
        jobDurability: true,
        jobRequestRecovery: false,
        jobMergeAllSnapshots: false,
        jobCollateSnapshots: false,
        useVolatility: false,
        jobParameters: [
            {
                name: 'RETAIL INSIDE__2',
                value: 'YEAR=%26'
            },
            {
                name: 'documentLabels',
                value: 'Sales analysis__1,RETAIL INSIDE__2,KPI_CARD__3'
            },
            {
                name: 'Sales analysis__1',
                value: 'hidden=%26'
            },
            {
                name: 'empty',
                value: 'empty'
            },
            {
                name: 'KPI_CARD__3',
                value: 'PRODUCT_FAMILY=%26YEAR=%26'
            }
        ],
        documents: [],
        numberOfDocuments: 0,
        triggers: []
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: { root: mockedJobs }
        })
    )
}

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: SchedulerHint
        },
        {
            path: '/scheduler',
            component: SchedulerHint
        },
        {
            path: '/new-package-schedule',
            component: null
        },
        {
            path: '/edit-package-schedule',
            props: true,
            component: null
        }
    ]
})

const $confirm = {
    require: vi.fn()
}

const $router = {
    push: vi.fn()
}

const $route = {
    query: {
        id: ''
    }
}

const factory = () => {
    return mount(Scheduler, {
        provide: [router, PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [router, createTestingPinia()],
            stubs: {
                Button,
                Card,
                FabButton,
                KnListBox: true,
                ProgressBar,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $confirm,
                $router,
                $route,
                $http
            }
        }
    })
}

beforeEach(async () => {
    router.push('/scheduler')
    await router.isReady()
})

afterEach(() => {
    vi.clearAllMocks()
})

describe('Scheduler loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })

    it('should show an hint in the detail when loaded empty', () => {
        const wrapper = factory()

        expect(wrapper.html()).toContain('managers.scheduler.hint')
    })

    it('loads job list correctly', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.jobs).toStrictEqual(mockedJobs)
    })
})
