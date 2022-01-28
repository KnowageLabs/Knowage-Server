import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import DataSourceManagement from './DataSourceManagement.vue'
import DataSourceManagementHint from './DataSourceManagementHint.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import KnHint from '@/components/UI/KnHint.vue'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'

const mockedDs = [
    {
        dsId: 1,
        id: 1,
        label: 'ds_cache',
        descr: 'ds_cache'
    },
    {
        id: 2,
        label: 'bi_demo',
        descr: 'bi_demo'
    },
    {
        id: 3,
        label: 'test_test',
        descr: 'test_test'
    }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasources`:
                return Promise.resolve({ data: mockedDs })
            default:
                return Promise.resolve({ data: [] })
        }
    }),
    post: axios.post.mockImplementation(() => Promise.resolve()),
    delete: axios.delete.mockImplementation(() => Promise.resolve())
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

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: DataSourceManagementHint
        },
        {
            path: '/datasource-management',
            component: DataSourceManagementHint
        },
        {
            path: '/datasource-management/new-datasource',
            name: 'new-datasource',
            component: null
        },
        {
            path: '/datasource-management/:id',
            name: 'edit-datasource',
            component: null
        }
    ]
})

const factory = () => {
    return mount(DataSourceManagement, {
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [router],
            stubs: {
                Button,
                InputText,
                ProgressBar,
                Toolbar,
                KnHint,
                DataSourceManagementHint,
                Card,
                Listbox
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
    router.push('/datasource-management')
    await router.isReady()
})

afterEach(() => {
    jest.clearAllMocks()
})

describe('Data Source management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('shows "no data" label when loaded empty', async () => {
        axios.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()
        await flushPromises()
        expect(wrapper.vm.datasources.length).toBe(0)
        expect(wrapper.find('[data-test="datasource-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Data Source management', () => {
    it('shows an hint if no item is selected from the list', () => {
        const wrapper = factory()

        expect(wrapper.html()).toContain('managers.dataSourceManagement.hint')
    })
    it('opens empty detail form when the ' + ' button is clicked', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect($router.push).toHaveBeenCalledWith('/datasource-management/new-datasource')
    })
    it('opens filled detail when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect(wrapper.vm.selDatasource).toStrictEqual({
            id: 1,
            dsId: 1,
            label: 'ds_cache',
            descr: 'ds_cache'
        })
        expect($router.push).toHaveBeenCalledWith('/datasource-management/1')
    })
})
