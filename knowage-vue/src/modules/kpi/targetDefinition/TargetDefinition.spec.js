import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import Listbox from 'primevue/listbox'
import ProgressBar from 'primevue/progressbar'
import TargetDefinition from './TargetDefinition.vue'
import Toolbar from 'primevue/toolbar'

const mockedTarget = [
    {
        id: 88,
        name: 'Test_target1',
        startValidity: 1625781600000,
        endValidity: 1625868000000,
        author: 'demo_admin',
        values: [],
        category: {
            valueId: 421,
            valueCd: 'SALES',
            valueName: 'SALES',
            valueDescription: 'SALES',
            domainCode: 'KPI_TARGET_CATEGORY',
            domainName: 'KPI_TARGET_CATEGORY',
            translatedValueName: 'SALES',
            translatedValueDescription: 'SALES'
        }
    },
    {
        id: 89,
        name: 'Test_target2',
        startValidity: 1625781600000,
        endValidity: 1625868000000,
        author: 'demo_admin',
        values: [],
        category: {
            valueId: 421,
            valueCd: 'SALES',
            valueName: 'SALES',
            valueDescription: 'SALES',
            domainCode: 'KPI_TARGET_CATEGORY',
            domainName: 'KPI_TARGET_CATEGORY',
            translatedValueName: 'SALES',
            translatedValueDescription: 'SALES'
        }
    },
    {
        id: 90,
        name: 'Test_target3',
        startValidity: 1625781600000,
        endValidity: 1625868000000,
        author: 'demo_admin',
        values: [],
        category: {
            valueId: 421,
            valueCd: 'SALES',
            valueName: 'SALES',
            valueDescription: 'SALES',
            domainCode: 'KPI_TARGET_CATEGORY',
            domainName: 'KPI_TARGET_CATEGORY',
            translatedValueName: 'SALES',
            translatedValueDescription: 'SALES'
        }
    }
]
jest.mock('axios', () => ({
    get: jest.fn(() => Promise.resolve({ data: mockedTarget })),
    delete: jest.fn(() => Promise.resolve())
}))

const $confirm = {
    require: jest.fn()
}

const $route = { path: '/business-model-catalogue' }

const $router = {
    push: jest.fn(),

    replace: jest.fn()
}

const $store = {
    commit: jest.fn()
}
const factory = () => {
    return mount(TargetDefinition, {
        global: {
            plugins: [],
            stubs: {
                Button,
                Card,
                InputText,
                Listbox,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $route,
                $router
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Target Definition loading', () => {
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

        expect(wrapper.vm.targetList.length).toBe(0)
        expect(wrapper.find('[data-test="target-list"]').html()).toContain('common.info.noDataFound')
    })
})
