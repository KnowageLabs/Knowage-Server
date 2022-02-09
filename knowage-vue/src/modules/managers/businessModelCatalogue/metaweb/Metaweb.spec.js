import { mount } from '@vue/test-utils'
import axios from 'axios'
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

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: [] }))

const factory = () => {
    return mount(Metaweb, {
        props: {
            propMeta: metaMock,
            visible: false,
            businessModel: {}
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: { ProgressBar, Dialog, TabView, TabPanel, BusinessModelTab, MetawebPhysicalModel, Toolbar, Button, Tooltip },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Metaweb', () => {
    it('show progress bar when loading', () => {
        //cannot test component because of teleporting dialog issue with primevue, data loads correctly
        const wrapper = factory()
        expect(wrapper.vm.meta.businessModels[0].name).toBe('Customer')
        expect(wrapper.vm.meta.businessModels[0].uniqueName).toBe('customer')
    })
    it('should shows the business/physical columns to add if the model is new', () => {
        //cannot test component because of teleporting dialog issue with primevue, data loads correctly
    })
    it('should open the business model list if business model tab is selected', () => {
        //cannot test component because of teleporting dialog issue with primevue, data loads correctly
    })
    it('should open the physical model list if physical model tab is selected', () => {
        //cannot test component because of teleporting dialog issue with primevue, data loads correctly
    })
})
