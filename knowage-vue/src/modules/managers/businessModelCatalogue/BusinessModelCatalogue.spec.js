import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import BusinessModelCatalogue from './BusinessModelCatalogue.vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnHint from '@/components/UI/KnHint.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedBusinessModels = [
    {
        id: 1,
        name: 'business model',
        description: 'lorem ipsum'
    },
    {
        id: 2,
        name: 'dummy',
        description: 'some description...'
    },
    {
        id: 3,
        name: 'mock',
        description: 'something'
    }
]

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedBusinessModels
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
    push: jest.fn(),
    replace: jest.fn()
}

const $route = { path: '/business-model-catalogue' }

const factory = () => {
    return mount(BusinessModelCatalogue, {
        global: {
            stubs: {
                Button,
                Card,
                FabButton,
                KnListBox: true,
                KnHint,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router,
                $route,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Business Model Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})

describe('Business Model Management', () => {
    it('shows an hint when no row is selected', () => {
        const wrapper = factory()

        expect(wrapper.vm.showHint).toBe(true)
        expect(wrapper.find('[data-test="bm-hint"]').exists()).toBe(true)
    })
    it("changes url when the when the '+' button is clicked", async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="new-button"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/business-model-catalogue/new-business-model')
    })
})
