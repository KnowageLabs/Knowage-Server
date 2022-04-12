import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import HierarchyManagement from './CalendarManagement.vue'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

const factory = () => {
    return mount(HierarchyManagement, {
        global: {
            plugins: [PrimeVue],
            stubs: { Button, HierarchyManagementMasterTab: true, HierarchyManagementTechnicalTab: true, HierarchyManagementBackupTab: true, KnOverlaySpinnerPanel, Toolbar },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Hierarchy management', () => {
    it('Should show a loader when opened', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="spinner"]').exists()).toBe(true)
    })
})
