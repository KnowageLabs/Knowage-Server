import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import LicenseDialog from './LicenseDialog.vue'
import LicenceTab from './LicenseTab.vue'
import ProgressBar from 'primevue/progressbar'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

jest.mock('axios', () => ({
    get: jest.fn(() => Promise.resolve({ data: [] }))
}))

const factory = () => {
    return mount(LicenseDialog, {
        global: {
            stubs: {
                Button,
                Dialog,
                LicenceTab,
                ProgressBar,
                TabView,
                TabPanel
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('License management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
    })
})
