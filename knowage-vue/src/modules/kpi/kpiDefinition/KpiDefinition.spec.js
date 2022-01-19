import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import KpiDefinition from './KpiDefinition.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedKpi = [
    {
        id: 1,
        name: 'PROFIT MARGIN',
        version: 1,
        dateCreation: 1477308600000
    },
    {
        id: 2,
        name: 'ROTATION',
        version: 2,
        dateCreation: 1477308600000
    },
    {
        id: 3,
        name: 'MARKUP',
        version: 3,
        dateCreation: 1477308600000
    }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedKpi
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
    push: jest.fn()
}

const factory = () => {
    return mount(KpiDefinition, {
        global: {
            stubs: {
                Button,
                InputText,
                ProgressBar,
                Toolbar,
                routerView: true
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

describe('Kpi Definition loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('shows "no data" label when loaded empty', async () => {
        axios.get.mockReturnValueOnce(Promise.resolve({ data: [] }))
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.kpiList.length).toBe(0)
        expect(wrapper.find('[data-test="kpi-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('KPI Definition List', () => {
    it('shows an hint if no item is selected from the list', () => {
        const wrapper = factory()
        expect(wrapper.vm.hintVisible).toBe(true)
    })
    it('deletes KPI when clicking on delete icon', async () => {
        const wrapper = factory()
        await flushPromises()

        expect(wrapper.vm.kpiList.length).toBe(3)

        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteKpi(1)
        expect(axios.delete).toHaveBeenCalledTimes(1)
    })
    it('changes url when the "+" button is clicked', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect($router.push).toHaveBeenCalledWith('/kpi-definition/new-kpi')
    })
    it('changes url with clicked row id and version when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/kpi-definition/' + 1 + '/' + 1)
    })
})
