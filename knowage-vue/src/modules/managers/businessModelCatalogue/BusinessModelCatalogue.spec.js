import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import BusinessModelCatalogue from './BusinessModelCatalogue.vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import Listbox from 'primevue/listbox'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedBusinessModels = [
    {
        id: 1,
        name: 'test',
        description: 'test'
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

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedBusinessModels }))
axios.delete.mockImplementation(() => Promise.resolve())

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
    return mount(BusinessModelCatalogue, {
        global: {
            stubs: {
                Button,
                FabButton,
                Listbox,
                ProgressBar,
                Toolbar,
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

describe('Business Model Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows "no data" label when loaded empty', async () => {
        axios.get.mockReturnValueOnce(Promise.resolve({ data: [] }))
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.businessModelList.length).toBe(0)
        expect(wrapper.find('[data-test="bm-list"]').html()).toContain('common.info.noDataFound')
    })
})
