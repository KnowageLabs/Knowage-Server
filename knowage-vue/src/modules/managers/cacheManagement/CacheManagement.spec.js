import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import CacheManagement from './CacheManagement.vue'
import flushPromises from 'flush-promises'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import mainStore from '../../../App.store'

const mockedCache = { totalMemory: 1073741824, availableMemory: 1073709056, availableMemoryPercentage: 100, cachedObjectsCount: 2, cleaningEnabled: true, cleaningQuota: '90%' }
const mockedDatasets = [
    {
        dsId: 1,
        label: 'ds_cache',
        readOnly: false,
        writeDefault: true
    },
    {
        dsId: 2,
        label: 'ds_test_oracle',
        readOnly: false,
        writeDefault: false
    },
    {
        dsId: 3,
        label: 'ds_test_postregsql',
        readOnly: true,
        writeDefault: false
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/cacheee':
                return Promise.resolve({ data: mockedCache })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasources/?type=cache':
                return Promise.resolve({ data: mockedDatasets })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

const factory = () => {
    return mount(CacheManagement, {
        global: {
            plugins: [createTestingPinia()],
            stubs: { DatasetTableCard: true, GeneralSettingsCard: true, ProgressBar, RuntimeInformationCard: true, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $http
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
    it('loads cache data', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.cache).toStrictEqual(mockedCache)
    })
    it('loads filtered datasources and selects correct one', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.datasources).toStrictEqual(mockedDatasets.slice(0, 2))
        expect(wrapper.vm.selectedDatasource).toStrictEqual(mockedDatasets[0])
    })
    it('loads filtered datasources and selects correct one', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.datasources).toStrictEqual(mockedDatasets.slice(0, 2))
        expect(wrapper.vm.selectedDatasource).toStrictEqual(mockedDatasets[0])
    })
    it('shows error dialog if there is no selected dataset returned', async () => {
        mockedDatasets[0].writeDefault = false
        const wrapper = factory()
        const store = mainStore()

        await flushPromises()

        expect(wrapper.vm.datasources).toStrictEqual(mockedDatasets.slice(0, 2))
        expect(store.setError).toHaveBeenCalledTimes(1)
        expect(wrapper.vm.selectedDatasource).toStrictEqual(null)
    })
})
