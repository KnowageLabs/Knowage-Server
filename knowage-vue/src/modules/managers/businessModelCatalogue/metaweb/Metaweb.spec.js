import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import Metaweb from './Metaweb.vue'
import ProgressBar from 'primevue/progressbar'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import BusinessModelTab from './businessModel/MetawebBusinessModel.vue'
import MetawebPhysicalModel from './physicalModel/MetawebPhysicalModel.vue'
import Toolbar from 'primevue/toolbar'
import Button from 'primevue/button'
import Tooltip from 'primevue/tooltip'
import metaMock from './businessModel/MetawebBusinessModelTestMock.json'

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}


const factory = () => {
    return mount(Metaweb, {
        props: {
            propMeta: metaMock,
            visible: false,
            businessModel: {}
        },
        global: {
            plugins: [createTestingPinia()],
            directives: {
                tooltip() {}
            },
            stubs: { ProgressBar, Dialog, TabView, TabPanel, BusinessModelTab, MetawebPhysicalModel, Toolbar, Button, Tooltip },
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

describe('Metaweb', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()
        expect(wrapper.vm.meta.businessModels[0].name).toBe('Customer')
        expect(wrapper.vm.meta.businessModels[0].uniqueName).toBe('customer')
    })
})
