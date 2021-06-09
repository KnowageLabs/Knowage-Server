import { mount } from '@vue/test-utils'
import CacheManagement from './CacheManagement.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

jest.mock('axios', () => ({
    get: jest.fn(() => Promise.resolve({ data: [] }))
}))

afterEach(() => {
    jest.clearAllMocks()
})

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(CacheManagement, {
        global: {
            plugins: [],
            stubs: { DatasetTableCard: true, GeneralSettingsCard: true, ProgressBar, RuntimeInformationCard: true, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

describe('Cache Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})
