import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import DataSourceManagement from './DataSourceManagement.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import KnHint from '@/components/UI/KnHint.vue'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'

const mockedDs = [
    {
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

jest.mock('axios', () => ({
    get: jest.fn(() =>
        Promise.resolve({
            data: mockedDs
        })
    ),
    delete: jest.fn(() => Promise.resolve()),
    post: jest.fn(() => Promise.resolve())
}))

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
    return mount(DataSourceManagement, {
        global: {
            stubs: {
                Button,
                InputText,
                ProgressBar,
                Toolbar,
                KnHint,
                Card,
                Listbox,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
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
        expect(wrapper.vm.hintVisible).toBe(true)
    })
    it('opens empty detail form when the ' + ' button is clicked', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect($router.push).toHaveBeenCalledWith('/datasource/new-datasource')
    })
    it('opens filled detail when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect(wrapper.vm.hintVisible).toBe(false)
        expect(wrapper.vm.selDatasource).toStrictEqual({
            id: 1,
            label: 'ds_cache',
            descr: 'ds_cache'
        })
        expect(wrapper.vm.disableLabelField).toBe(true)
    })
})
