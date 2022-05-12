import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
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
    it('shows "no data" label when loaded empty', async () => {
        axios.get.mockReturnValueOnce(Promise.resolve({ data: [] }))
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.roles.length).toBe(0)
        expect(wrapper.find('[data-test="roles-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Roles Management', () => {
    it('deletes role after clicking on delete icon', async () => {
        const wrapper = factory()
        await flushPromises()

        expect(wrapper.vm.roles.length).toBe(3)

        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteRole(1)
        expect(axios.delete).toHaveBeenCalledTimes(1)
        expect(axios.delete).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles/' + 1, { headers: { 'X-Disable-Errors': 'true' } })
    })
    it('changes url when the "+" button is clicked', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect($router.push).toHaveBeenCalledWith('/roles-management/new-role')
    })
    it('changes url with clicked row id when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/roles-management/' + 1)
    })
})

describe('Roles Management Search', () => {
    it('filters the list if a label (or other column) is provided', async () => {
        const wrapper = factory()
        await flushPromises()
        const rolesList = wrapper.find('[data-test="roles-list"]')
        const searchInput = rolesList.find('input')

        expect(rolesList.html()).toContain('/kte/admin')
        expect(rolesList.html()).toContain('user')
        expect(rolesList.html()).toContain('dev')

        // Name
        await searchInput.setValue('user')
        await rolesList.trigger('filter')
        expect(rolesList.html()).not.toContain('/kte/admin')
        expect(rolesList.html()).toContain('user')
        expect(rolesList.html()).not.toContain('dev')

        // Role Type
        await searchInput.setValue('TEST_ROLE')
        await rolesList.trigger('filter')
        expect(rolesList.html()).not.toContain('/kte/admin')
        expect(rolesList.html()).toContain('user')
        expect(rolesList.html()).not.toContain('dev')
    })
    it('returns no data if the label is not present', async () => {
        const wrapper = factory()
        await flushPromises()
        const rolesList = wrapper.find('[data-test="roles-list"]')
        const searchInput = rolesList.find('input')

        expect(rolesList.html()).toContain('/kte/admin')
        expect(rolesList.html()).toContain('user')
        expect(rolesList.html()).toContain('dev')

        await searchInput.setValue('not present value')
        await rolesList.trigger('filter')

        expect(rolesList.html()).not.toContain('/kte/admin')
        expect(rolesList.html()).not.toContain('user')
        expect(rolesList.html()).not.toContain('dev')
    })
})
