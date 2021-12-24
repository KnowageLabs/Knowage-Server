import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import Olap from './Olap.vue'
import ProgressBar from 'primevue/progressbar'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Toolbar from 'primevue/toolbar'
import Tooltip from 'primevue/tooltip'

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() => Promise.resolve({ data: [] })),
    post: axios.get.mockImplementation(() => Promise.resolve({ data: [] }))
}

const factory = () => {
    return mount(Olap, {
        props: {},
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Button,
                FilterPanel: true,
                FilterTopToolbar: true,
                FilterLeftToolbar: true,
                KnOverlaySpinnerPanel,
                MultiHierarchyDialog: true,
                OlapSidebar: true,
                OlapSortingDialog: true,
                OlapCustomViewTable: true,
                OlapCustomViewSaveDialog: true,
                OlapMDXQueryDialog: true,
                OlapCrossNavigationDefinitionDialog: true,
                OlapButtonWizardDialog: true,
                ProgressBar,
                TabView,
                TabPanel,
                Toolbar,
                Tooltip
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Olap', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()
        console.log(wrapper.html())
    })
})
