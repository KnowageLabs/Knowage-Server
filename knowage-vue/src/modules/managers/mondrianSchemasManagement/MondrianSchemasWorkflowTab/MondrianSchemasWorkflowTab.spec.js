import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import MondrianSchemasWorkflowTab from './MondrianSchemasWorkflowTab.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedUsers = [
    {
        id: 1,
        name: 'bitest',
        description: 'Knowage Test User'
    },
    {
        id: 2,
        name: 'wolelo',
        description: 'Random User'
    },
    {
        id: 3,
        name: 'Test 123',
        description: 'Another Test User'
    }
]

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedUsers }))
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
    return mount(MondrianSchemasWorkflowTab, {
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
                $router
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Mondrian Schema Workflow Tab', () => {
    it('clicking on an left side user it will be put in the right side', async () => {})
    it('clicking on an right side user it will be put in the left side', async () => {})
})
