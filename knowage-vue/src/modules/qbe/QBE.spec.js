import { mount, flushPromises } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Chip from 'primevue/chip'
import InputSwitch from 'primevue/inputswitch'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import ScrollPanel from 'primevue/scrollpanel'
import Menu from 'primevue/contextmenu'
import QBE from './QBE.vue'
import ProgressBar from 'primevue/progressbar'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Toolbar from 'primevue/toolbar'
import Tooltip from 'primevue/tooltip'

const mockedQBE = {}

jest.mock('axios')
const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            default:
                return Promise.resolve({ data: [] })
        }
    }),

    post: axios.post.mockImplementation((url) => {
        switch (url) {
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $route = { name: '' }

const $store = {
    state: {
        user: {}
    }
}

const factory = () => {
    return mount(QBE, {
        props: {
            id: '1',
            olapId: '1'
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Button,
                Chip,
                KnOverlaySpinnerPanel,
                InputSwitch,
                Menu,
                ProgressBar,
                ScrollPanel,
                TabView,
                TabPanel,
                Toolbar,
                Tooltip,
                QBEAdvancedFilterDialog: true,
                QBEFilterDialog: true,
                QBEHavingDialog: true,
                QBESimpleTable: true,
                QBESqlDialog: true,
                QBERelationDialog: true,
                QBEParamDialog: true,
                QBESavingDialog: true,
                QBESmartTable: true,
                ExpandableEntity: true,
                SubqueryEntity: true,
                QBEJoinDefinitionDialog: true,
                KnParameterSidebar: true,
                QBEPreviewDialog: true
            },
            mocks: {
                $t: (msg) => msg,
                $http,
                $route
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('QBE', () => {
    it('shows progress bar when loading', async () => {
        const wrapper = factory()

        console.log(wrapper.html())
    })
})
