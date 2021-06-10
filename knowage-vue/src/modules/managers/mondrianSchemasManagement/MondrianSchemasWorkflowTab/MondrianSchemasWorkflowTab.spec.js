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
        userId: 'bitest',
        fullName: 'Knowage Test User'
    },
    {
        id: 2,
        userId: 'biadmin',
        fullName: 'Knowage Administrator'
    },
    {
        id: 3,
        name: 'Test 123',
        userId: 'mbalestri',
        fullName: 'MARCO BALESTRI'
    }
]

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
    it("shows 'no data' label when loaded empty", () => {
        const wrapper = factory([], [])

        expect(wrapper.props('availableUsersList[0]').length).toBe(0)

        expect(wrapper.find('[data-test="userList1-list"]').html()).toContain('common.info.noDataFound')
    })
    it('clicking on an left side user it will be put in the right side', async () => {})
    it('clicking on an right side user it will be put in the left side', async () => {})
})
