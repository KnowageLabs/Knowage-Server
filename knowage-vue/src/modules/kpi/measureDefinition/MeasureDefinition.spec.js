import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import KnHint from '@/components/UI/KnHint.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import MeasureDefinition from './MeasureDefinition.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedMeasures = [
    {
        id: 1,
        alias: 'd_profit',
        rule: 'DEMO',
        ruleId: 1,
        ruleVersion: 1,
        category: {
            valueCd: 'SALES'
        },
        author: 'demo_admin'
    },
    {
        id: 2,
        alias: 'd_profittability',
        rule: 'BOJAN',
        ruleId: 2,
        ruleVersion: 1,
        category: {
            valueCd: 'SALES'
        },
        author: 'demo_admin'
    },
    {
        id: 3,
        alias: 'd_store_cost',
        rule: 'BOJAN',
        ruleId: 2,
        ruleVersion: 2,
        category: {
            valueCd: 'SALES'
        },
        author: 'demo_admin'
    }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedMeasures
        })
    ),
    delete: axios.delete.mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn(),
    replace: jest.fn()
}

const factory = () => {
    return mount(MeasureDefinition, {
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Button,
                Card,
                FabButton,
                InputText,
                Listbox,
                KnHint,
                KnFabButton,
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

afterEach(() => {
    jest.clearAllMocks()
})

describe('Measure Definition loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows an hint component when loaded empty', async () => {
        axios.get.mockReturnValueOnce(Promise.resolve({ data: [] }))
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.measuresList.length).toBe(0)
        expect(wrapper.find('[data-test="measure-hint"]').exists()).toBe(true)
    })
})

describe('Measure Definition', () => {
    it('shows a prompt when user click on a rule delete button to delete it and deletes it', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.measuresList.length).toBe(3)

        await wrapper.find('[data-test="delete-button-1"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteMeasure(mockedMeasures[0])
        expect(axios.delete).toHaveBeenCalledTimes(1)
        expect(axios.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/kpi/1/1/deleteRule')
        expect($store.commit).toHaveBeenCalledTimes(1)
    })
    it('calls the correct route when clicking on the add button', async () => {
        const wrapper = factory()

        await flushPromises()

        await wrapper.find('[data-test="new-button"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/measure-definition/new-measure-definition')
    })
    it('calls the correct route when clicking on a row', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('[data-test="measures-table"] tr td').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/measure-definition/edit?id=1&ruleVersion=1&clone=false')
    })
})
