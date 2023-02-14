import { flushPromises, mount } from '@vue/test-utils'
import { vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import MetawebBusinessModel from './MetawebBusinessModel.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Menu from 'primevue/contextmenu'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Chip from 'primevue/chip'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import Button from 'primevue/button'
import mainDescriptor from '../MetawebDescriptor.json'
import bmDescriptor from './MetawebBusinessModelDescriptor.json'
import metaMock from './MetawebBusinessModelTestMock.json'


vi.mock('axios')
const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/roles`:
                return Promise.resolve({ data: [] })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const factory = () => {
    return mount(MetawebBusinessModel, {
        props: {
            propMeta: metaMock,
            metaUpdated: false,
            observer: {}
        },
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                KnFabButton,
                TabView,
                TabPanel,
                Menu,
                MetawebBusinessPropertyListTab: true,
                BusinessClassDialog: true,
                BusinessViewDialog: true,
                MetawebAttributesTab: true,
                InboundRelationships: true,
                OutboundRelationships: true,
                MetawebPhysicalTableTab: true,
                MetawebJoinRelationships: true,
                MetawebFilterTab: true,
                Column,
                DataTable,
                Chip,
                ProgressBar,
                Toolbar,
                Button,
                mainDescriptor,
                bmDescriptor
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Metaweb Business Model', () => {
    it('clicking on a business model in the list the detail should open in the detail section', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()
        await flushPromises()

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual({})
        const dataTable = wrapper.find('[data-test="bm-table"]')
        await dataTable.find('tr td').trigger('click')

        expect(wrapper.vm.selectedBusinessModel).toStrictEqual(metaMock.businessModels[0])
    })
    it('clicking on a business model in the list the detail should have the right number of available tabs', async () => {
        const wrapper = factory()

        await flushPromises()
        const dataTable = wrapper.find('[data-test="bm-table"]')
        await dataTable.find('tr td').trigger('click')

        expect(wrapper.find('.p-tabview-nav li:nth-child(6)').html()).toContain('metaweb.businessModel.tabView.filter')
    })
})
