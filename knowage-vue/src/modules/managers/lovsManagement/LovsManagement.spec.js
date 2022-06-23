import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import FabButton from '@/components/UI/KnFabButton.vue'
import LovesManagementHint from './LovesManagementHint.vue'
import flushPromises from 'flush-promises'
import LovsManagement from './LovsManagement.vue'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedLovs = [
    {
        id: 1,
        description: 'TST_AgeRange_Default',
        label: 'TST_AgeRange_Default',
        name: 'TST_AgeRange_Default',
        itypeCd: 'FIX_LOV'
    },
    {
        id: 2,
        description: 'Current month of the year format mm',
        label: 'CURRENT_MONTH_YEAR',
        name: 'CURRENT_MONTH_YEAR',
        itypeCd: 'SCRIPT'
    },
    {
        id: 3,
        description: 'DEMO_ProductFamily',
        label: 'DEMO_ProductFamily',
        name: 'DEMO_ProductFamily',
        itypeCd: 'QUERY'
    }
]

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedLovs
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
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
            component: LovesManagementHint
        },
        {
            path: '/lovs-management',
            component: LovesManagementHint
        },
        {
            path: '/lovs-management/new-lov',
            component: null
        },
        {
            path: '/lovs-management/:id',
            props: true,
            component: null
        }
    ]
})

const factory = () => {
    return mount(LovsManagement, {
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [router, PrimeVue],
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
                $store,
                $confirm,
                $router,
                $http
            }
        }
    })
}

beforeEach(async () => {
    router.push('/lovs-management')
    await router.isReady()
})

afterEach(() => {
    jest.clearAllMocks()
})

describe('Lovs Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows "no data" label when loaded empty', async () => {
        axios.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.lovsList.length).toBe(0)
    })
    it('shows an hint when no item is selected', async () => {
        await flushPromises()

        const wrapper = factory()

        expect(wrapper.html()).toContain('managers.lovsManagement.hint')
    })
})

describe('Lovs Management', () => {
    it('shows a prompt when user click on a lov delete button to delete it and deletes it when deleteLov is called', async () => {
        const wrapper = factory()
        await flushPromises()

        await wrapper.vm.deleteLov(1)
        expect($http.delete).toHaveBeenCalledTimes(1)
        expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/lovs/delete/1')
        expect($router.push).toHaveBeenCalledWith('/lovs-management')
    })
    it("changes url when the when the ' + ' button is clicked", async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="new-button"]').trigger('click')

        await flushPromises()

        expect($router.push).toHaveBeenCalledWith('/lovs-management/new-lov')
    })
    it('changes url with clicked LOV id when a row is clicked', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.vm.showForm(mockedLovs[0])

        expect($router.push).toHaveBeenCalledWith('/lovs-management/2')
    })
})
