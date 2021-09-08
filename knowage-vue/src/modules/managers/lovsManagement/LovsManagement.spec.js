import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import FabButton from '@/components/UI/KnFabButton.vue'
import LovesManagementHint from './LovesManagementHint.vue'
import flushPromises from 'flush-promises'
import Listbox from 'primevue/listbox'
import LovsManagement from './LovsManagement.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedLovs = [
    {
        id: 1,
        description: 'TST_AgeRange_Default',
        label: 'TST_AgeRange_Default',
        name: 'TST_AgeRange_Default',
        itypeCd: 'FIX_LOV'
    },
    {
        id: 2,
        description: 'Current month of the year format mm',
        label: 'CURRENT_MONTH_YEAR',
        name: 'CURRENT_MONTH_YEAR',
        itypeCd: 'SCRIPT'
    },
    {
        id: 3,
        description: 'DEMO_ProductFamily',
        label: 'DEMO_ProductFamily',
        name: 'DEMO_ProductFamily',
        itypeCd: 'QUERY'
    }
]

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedLovs }))
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

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: LovesManagementHint
        },
        {
            path: '/lovs-management',
            component: LovesManagementHint
        },
        {
            path: '/lovs-management/new-lov',
            component: null
        },
        {
            path: '/lovs-management/:id',
            props: true,
            component: null
        }
    ]
})

const factory = () => {
    return mount(LovsManagement, {
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [router],
            stubs: {
                Button,
                Card,
                FabButton,
                Listbox,
                ProgressBar,
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

beforeEach(async () => {
    router.push('/lovs-management')
    await router.isReady()
})

afterEach(() => {
    jest.clearAllMocks()
})

describe('Lovs Management loading', () => {
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

        expect(wrapper.vm.lovsList.length).toBe(0)
        expect(wrapper.find('[data-test="lovs-list"]').html()).toContain('common.info.noDataFound')
    })
    it('shows an hint when no item is selected', async () => {
        await flushPromises()

        const wrapper = factory()

        expect(wrapper.html()).toContain('managers.lovsManagement.hint')
    })
})

describe('Lovs Management', () => {
    it('shows a prompt when user click on a lov delete button to delete it and deletes it when deleteLov is called', async () => {
        const wrapper = factory()
        await flushPromises()

        await wrapper.find('[data-test="delete-button-1"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteLov(1)
        expect(axios.delete).toHaveBeenCalledTimes(1)
        expect(axios.delete).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/delete/1')
        expect($router.push).toHaveBeenCalledWith('/lovs-management')
    })
    it("changes url when the when the ' + ' button is clicked", async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="new-button"]').trigger('click')

        await flushPromises()

        expect($router.push).toHaveBeenCalledWith('/lovs-management/new-lov')
    })
    it('changes url with clicked LOV id when a row is clicked', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('[data-test="list-item-1"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/lovs-management/1')
    })
})
