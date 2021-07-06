import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import BusinessModelCatalogue from './BusinessModelCatalogue.vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import KnHint from '@/components/UI/KnHint.vue'
import Listbox from 'primevue/listbox'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedBusinessModels = [
    {
        id: 1,
        name: 'business model',
        description: 'lorem ipsum'
    },
    {
        id: 2,
        name: 'dummy',
        description: 'some description...'
    },
    {
        id: 3,
        name: 'mock',
        description: 'something'
    }
]

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedBusinessModels }))
axios.delete.mockImplementation(() => Promise.resolve())

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

const $route = { path: '/business-model-catalogue' }

const factory = () => {
    return mount(BusinessModelCatalogue, {
        global: {
            stubs: {
                Button,
                Card,
                FabButton,
                Listbox,
                KnHint,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router,
                $route
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Business Model Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows "no data" label when loaded empty', async () => {
        axios.get.mockReturnValueOnce(Promise.resolve({ data: [] }))
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.businessModelList.length).toBe(0)
        expect(wrapper.find('[data-test="bm-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Business Model Management', () => {
    it('shows an hint when no row is selected', () => {
        const wrapper = factory()

        expect(wrapper.vm.showHint).toBe(true)
        expect(wrapper.find('[data-test="bm-hint"]').exists()).toBe(true)
    })
    it('deletes schema clicking on delete icon', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.businessModelList.length).toBe(3)

        await wrapper.find('[data-test="delete-button"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteBusinessModel(1)
        expect(axios.delete).toHaveBeenCalledTimes(1)
        expect(axios.delete).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/businessmodels/' + 1)
        expect($store.commit).toHaveBeenCalledTimes(1)
        expect($router.replace).toHaveBeenCalledWith('/business-model-catalogue')
    })
    it("changes url when the when the '+' button is clicked", async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="new-button"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/business-model-catalogue/new-business-model')
    })
    it('changes url with clicked row id when a row is clicked', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/business-model-catalogue/' + 1)
    })
})

describe('Business Model Management Search', () => {
    it('filters the list if a name (or description) is provided', async () => {
        const wrapper = factory()
        await flushPromises()
        const businessModelList = wrapper.find('[data-test="bm-list"]')
        const searchInput = businessModelList.find('input')

        expect(businessModelList.html()).toContain('business model')
        expect(businessModelList.html()).toContain('dummy')
        expect(businessModelList.html()).toContain('mock')

        // Name
        await searchInput.setValue('dummy')
        await businessModelList.trigger('filter')
        expect(businessModelList.html()).not.toContain('business model')
        expect(businessModelList.html()).toContain('dummy')
        expect(businessModelList.html()).not.toContain('mock')

        // Description
        await searchInput.setValue('something')
        await businessModelList.trigger('filter')
        expect(businessModelList.html()).not.toContain('business model')
        expect(businessModelList.html()).not.toContain('dummy')
        expect(businessModelList.html()).toContain('mock')
    })
    it('returns no data if the Name is not present', async () => {
        const wrapper = factory()
        await flushPromises()
        const businessModelList = wrapper.find('[data-test="bm-list"]')
        const searchInput = businessModelList.find('input')

        expect(businessModelList.html()).toContain('business model')
        expect(businessModelList.html()).toContain('dummy')
        expect(businessModelList.html()).toContain('mock')

        await searchInput.setValue('not present value')
        await businessModelList.trigger('filter')
        expect(businessModelList.html()).not.toContain('business model')
        expect(businessModelList.html()).not.toContain('dummy')
        expect(businessModelList.html()).not.toContain('mock')
    })
})
