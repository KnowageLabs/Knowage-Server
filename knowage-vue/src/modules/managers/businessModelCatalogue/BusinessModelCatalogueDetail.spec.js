import { mount } from '@vue/test-utils'
import axios from 'axios'
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

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/1`:
                return Promise.resolve({ data: mockedBusinessModel })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/1/versions/`:
                return Promise.resolve({ data: { versions: [] } })
            default:
                return Promise.resolve({ data: [] })
        }
    }),
    post: axios.post.mockImplementation(() => Promise.resolve())
}

// axios.get.mockImplementation((url) => {
//     switch (url) {
//         case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/1`:
//             return Promise.resolve({ data: mockedBusinessModel })
//         case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/1/versions/`:
//             return Promise.resolve({ data: { versions: [] } })
//         default:
//             return Promise.resolve({ data: [] })
//     }
// })
// axios.post.mockImplementation(() => Promise.resolve())

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn(),
    replace: jest.fn()
}

const factory = () => {
    return mount(BusinessModelCatalogueDetail, {
        global: {
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
                $store,
                $router,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
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
        expect(axios.post).toHaveBeenCalledTimes(0)
    })
})

describe('Business Model Catalogue Detail', () => {
    it('clicking on metadata tab the metadata page is opened', async () => {
        const wrapper = factory()

        await wrapper.setProps({ id: 1 })
        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(2) a').trigger('click')

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual({ ...mockedBusinessModel, category: undefined })
        expect(wrapper.find('.p-tabview-nav li:nth-child(2)').html()).toContain('aria-selected="true"')
    })
    it('clicking on saved versions tab the saved version page is opened', async () => {
        const wrapper = factory()

        await wrapper.setProps({ id: 1 })
        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(3) a').trigger('click')

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual({ ...mockedBusinessModel, category: undefined })
        expect(wrapper.find('.p-tabview-nav li:nth-child(3)').html()).toContain('aria-selected="true"')
    })
    it('clicking on drivers tab the drivers page is opened', async () => {
        const wrapper = factory()

        await wrapper.setProps({ id: 1 })
        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(4) a').trigger('click')

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual({ ...mockedBusinessModel, category: undefined })
        expect(wrapper.find('.p-tabview-nav li:nth-child(4)').html()).toContain('aria-selected="true"')
    })
})
