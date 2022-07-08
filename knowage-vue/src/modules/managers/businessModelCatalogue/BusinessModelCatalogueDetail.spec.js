import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import Badge from 'primevue/badge'
import Button from 'primevue/button'
import BusinessModelCatalogueDetail from './BusinessModelCatalogueDetail.vue'
import InputText from 'primevue/inputtext'
import flushPromises from 'flush-promises'
import ProgressBar from 'primevue/progressbar'
import TabPanel from 'primevue/tabpanel'
import TabView from 'primevue/tabview'
import Toolbar from 'primevue/toolbar'

const mockedBusinessModel = {
    id: 1,
    name: 'test',
    description: 'test'
}

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/businessmodels/1`:
                return Promise.resolve({ data: mockedBusinessModel })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/businessmodels/1/versions/`:
                return Promise.resolve({ data: { versions: [] } })
            default:
                return Promise.resolve({ data: [] })
        }
    }),
    post: vi.fn().mockImplementation(() => Promise.resolve())
}

const $router = {
    push: vi.fn(),
    replace: vi.fn()
}

const factory = (id) => {
    return mount(BusinessModelCatalogueDetail, {
        props: {
            id: id
        },
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                Badge,
                BusinessModelDetailsCard: true,
                BusinessModelDriversCard: true,
                BusinessModelVersionsCard: true,
                Button,
                ProgressBar,
                InputText,
                TabPanel,
                TabView,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $router,
                $http
            }
        }
    })
}

window.HTMLElement.prototype.scrollIntoView = function() {}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Business Model Catalogue Detail', () => {
    it('save button is disabled if a mandatory input is empty', () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual({})
        expect(wrapper.vm.buttonDisabled).toBe(true)
        expect(wrapper.find('[data-test="submit-button"]').element.disabled).toBe(true)
    })
    it('close button (X) closes the detail without saving data', async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="close-button"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/business-model-catalogue')
        expect(wrapper.emitted().closed).toBeTruthy()
        expect($http.post).toHaveBeenCalledTimes(0)
    })
})

describe('Business Model Catalogue Detail', () => {
    it('clicking on metadata tab the metadata page is opened', async () => {
        const wrapper = factory(1)
        
        await flushPromises()
        console.log(' >>>>>>>>>>>> ', wrapper.find('.p-tabview-nav'))
        await wrapper.find('.p-tabview-nav li:nth-child(2) a').trigger('click')

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual({ ...mockedBusinessModel, category: undefined })
        expect(wrapper.find('.p-tabview-nav li:nth-child(2)').html()).toContain('aria-selected="true"')
    })
    it('clicking on saved versions tab the saved version page is opened', async () => {
        const wrapper = factory(1)

        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(3) a').trigger('click')

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual({ ...mockedBusinessModel, category: undefined })
        expect(wrapper.find('.p-tabview-nav li:nth-child(3)').html()).toContain('aria-selected="true"')
    })
    it('clicking on drivers tab the drivers page is opened', async () => {
        const wrapper = factory(1)

        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(4) a').trigger('click')

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual({ ...mockedBusinessModel, category: undefined })
        expect(wrapper.find('.p-tabview-nav li:nth-child(4)').html()).toContain('aria-selected="true"')
    })
})
