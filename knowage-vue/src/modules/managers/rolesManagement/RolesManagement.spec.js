import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import RolesManagement from './RolesManagement.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedRoles = [
    {
        id: 1,
        name: '/kte/admin',
        description: '/kte/admin',
        roleTypeCD: 'ADMIN',
        code: null,
        roleTypeID: 28,
        isPublic: false
    },
    {
        id: 2,
        name: 'user',
        description: 'user',
        roleTypeCD: 'TEST_ROLE',
        code: '1234',
        roleTypeID: 28,
        isPublic: true
    },
    {
        id: 3,
        name: 'dev',
        description: 'dev',
        roleTypeCD: 'ADMIN',
        code: '9999',
        roleTypeID: 28,
        isPublic: false
    }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedRoles
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
    return mount(RolesManagement, {
        global: {
            stubs: {
                Button,
                InputText,
                KnListBox: true,
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

describe('Roles Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})

describe('Roles Management', () => {
    it('changes url when the "+" button is clicked', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect($router.push).toHaveBeenCalledWith('/roles-management/new-role')
    })
})
