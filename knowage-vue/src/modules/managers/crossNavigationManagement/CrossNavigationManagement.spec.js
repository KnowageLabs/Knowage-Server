import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import flushPromises from 'flush-promises'
import CrossNavigationManagementHint from './CrossNavigationManagementHint.vue'
import Toolbar from 'primevue/toolbar'
import ProgressBar from 'primevue/progressbar'
import crossNavigationManagement from './CrossNavigationManagement.vue'
import PrimeVue from 'primevue/config'

const mockedNavigations = [
    {
        id: 655,
        name: 'MapToRPTfile',
        description: null,
        breadcrumb: null,
        type: 0,
        fromDoc: 'DEMO_Report',
        fromDocId: 2341,
        toDoc: 'DEMO_Report',
        toDocId: 2341,
        fixedValue: null,
        popupOptions: null
    },
    {
        id: 640,
        name: 'svgStateToReport',
        description: null,
        breadcrumb: null,
        type: 0,
        fromDoc: 'DM_SVG_STORE2',
        fromDocId: null,
        toDoc: 'BestProductSingPar',
        toDocId: null,
        fixedValue: null,
        popupOptions: null
    },
    {
        id: 673,
        name: 'Test',
        description: 'Test description',
        breadcrumb: 'test text',
        type: 1,
        fromDoc: 'KPI_LIST',
        fromDocId: 2376,
        toDoc: 'Accident_NYC',
        toDocId: 3219,
        fixedValue: null,
        popupOptions: null
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: CrossNavigationManagementHint
        },
        {
            path: '/cross-navigation-management',
            component: CrossNavigationManagementHint
        },
        {
            path: '/cross-navigation-management/new-navigation',
            component: null
        },
        {
            path: '/cross-navigation-management/:id',
            props: true,
            component: null
        }
    ]
})

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedNavigations
        })
    ),
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

const factory = () => {
    return mount(crossNavigationManagement, {
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [router, PrimeVue],
            stubs: {
                Button,
                Card,
                KnFabButton,
                KnListBox,
                Listbox,
                ProgressBar,
                Toolbar
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

beforeEach(async () => {
    router.push('/cross-navigation-management')
    await router.isReady()
})

afterEach(() => {
    jest.clearAllMocks()
})

describe('Cross-navigation Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})
describe('Cross-navigation Management', () => {
    it('shows a prompt when user click on a list item delete button to delete it', async () => {
        const wrapper = factory()
        await flushPromises()

        wrapper.vm.deleteTempateConfirm({ item: { id: 655 } }, 655)
        expect($confirm.require).toHaveBeenCalledTimes(1)
    })
    it('shows and empty detail when clicking on the add button', async () => {
        const wrapper = factory()
        await wrapper.find('[data-test="new-button"]').trigger('click')
        await flushPromises()
        expect($router.push).toHaveBeenCalledWith('/cross-navigation-management/new-navigation')
    })
    it('shows the detail when clicking on a item', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')
        expect($router.push).toHaveBeenCalledWith('/cross-navigation-management/655')
    })
})
