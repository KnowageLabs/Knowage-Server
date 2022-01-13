import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import MondrianSchemasWorkflowTab from './MondrianSchemasWorkflowTab.vue'
import Toolbar from 'primevue/toolbar'

const mockedUsers = [
    [
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
    ],
    [
        {
            id: 4,
            userId: 'mitest',
            fullName: 'Misto Test'
        }
    ]
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

const factory = (usersList) => {
    return mount(MondrianSchemasWorkflowTab, {
        props: {
            usersList
        },
        global: {
            stubs: {
                Button,
                Toolbar
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
        const wrapper = factory([])
        expect(wrapper.vm.availableUsersList.length).toBe(0)

        expect(wrapper.find('[data-test="userList1"]').html()).toContain('common.info.noDataFound')
        expect(wrapper.find('[data-test="userList2"]').html()).toContain('common.info.noDataFound')
    })
    it('clicking on an left side user it will be put in the right side', async () => {
        const wrapper = factory(mockedUsers)
        await wrapper.setData(mockedUsers)

        const leftList = wrapper.find('[data-test="userList1-item"]')
        await leftList.trigger('click')

        expect(wrapper.vm.availableUsersList[0].length).toBe(2)
        expect(wrapper.vm.availableUsersList[1].length).toBe(2)
    })
    it('clicking on an right side user it will be put in the left side', async () => {
        const wrapper = factory(mockedUsers)
        await wrapper.setData(mockedUsers)

        const rightList = wrapper.find('[data-test="userList2-item"]')
        await rightList.trigger('click')

        expect(wrapper.vm.availableUsersList[0].length).toBe(3)
        expect(wrapper.vm.availableUsersList[1].length).toBe(1)
    })
})
