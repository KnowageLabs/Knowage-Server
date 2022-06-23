import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import HierarchyManagement from './HierarchyManagement.vue'
import ProgressSpinner from 'primevue/progressspinner'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() => Promise.resolve({ data: [] }))
}

const factory = () => {
    return mount(HierarchyManagement, {
        global: {
            plugins: [PrimeVue],
            stubs: { Button, HierarchyManagementMasterTab: true, HierarchyManagementTechnicalTab: true, HierarchyManagementBackupTab: true, ProgressSpinner, Toolbar },
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

describe('Hierarchy Management', () => {
    it('Should show a loader when opened', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="spinner"]').exists()).toBe(true)
    })
})
