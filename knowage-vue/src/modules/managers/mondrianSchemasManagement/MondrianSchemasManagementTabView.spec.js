import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Column from 'primevue/column'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import MondrianSchemasManagementTabView from './MondrianSchemasManagementTabView.vue'
import ProgressBar from 'primevue/progressbar'
import TabPanel from 'primevue/tabpanel'
import TabView from 'primevue/tabview'
import Toolbar from 'primevue/toolbar'

const mockedSchema = {
    id: 1,
    name: 'Main Schema',
    description: 'big bonk',
    type: 'MONDRIAN_SCHEMA'
}

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedSchema }))

axios.post.mockImplementation(() => Promise.resolve())

const $store = {
    commit: jest.fn()
}

const $router = {
    replace: jest.fn()
}

const factory = () => {
    return mount(MondrianSchemasManagementTabView, {
        global: {
            stubs: {
                Button,
                Column,
                Card,
                DataTable,
                KnValidationMessages,
                InputText,
                ProgressBar,
                TabPanel,
                TabView,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $router
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Mondrian Schema Management Tab View', () => {
    it('switches to Workflow tab if Workflow  is clicked', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(2)').trigger('click')
    })

    it('save button is disabled if a mandatory input is empty', () => {
        const wrapper = factory()
        expect(wrapper.vm.selectedSchema).toStrictEqual({})
        expect(wrapper.vm.buttonDisabled).toBe(true)
    })
})
