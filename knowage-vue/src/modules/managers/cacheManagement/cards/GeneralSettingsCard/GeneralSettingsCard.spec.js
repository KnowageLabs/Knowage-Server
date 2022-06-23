import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import flushPromises from 'flush-promises'
import GeneralSettingsCard from './GeneralSettingsCard.vue'
import Toolbar from 'primevue/toolbar'

const mockedDatasets = [
    {
        dsId: 1,
        label: 'ds_cache'
    },
    {
        dsId: 2,
        label: 'ds_test_oracle'
    },
    {
        dsId: 3,
        label: 'ds_test_postregsql'
    }
]

const mockedSettings = {
    prefixForCacheTablesName: 'sbicache',
    spaceAvailable: 1024,
    limitForClean: 90,
    schedulingFullClean: { label: 'DAILY', value: 'DAILY' },
    lastAccessTtl: 600,
    createAndPersistTimeout: 120,
    cacheLimitForStore: 10,
    sqldbCacheTimeout: 180000,
    hazelcastTimeout: 120,
    hazelcastLeaseTime: 240
}

const expectedConfiguration = [
    {
        label: 'SPAGOBI.CACHE.NAMEPREFIX',
        value: mockedSettings.prefixForCacheTablesName
    },
    {
        label: 'SPAGOBI.CACHE.SPACE_AVAILABLE',
        value: mockedSettings.spaceAvailable * 1048576
    },
    {
        label: 'SPAGOBI.CACHE.LIMIT_FOR_CLEAN',
        value: mockedSettings.limitForClean
    },
    {
        label: 'SPAGOBI.CACHE.SCHEDULING_FULL_CLEAN',
        value: mockedSettings.schedulingFullClean.value,
        id: mockedSettings.schedulingFullClean.value
    },
    {
        label: 'SPAGOBI.CACHE.LIMIT_FOR_STORE',
        value: mockedSettings.cacheLimitForStore
    },
    {
        label: 'SPAGOBI.CACHE.DS_LAST_ACCESS_TTL',
        value: mockedSettings.lastAccessTtl
    },
    {
        label: 'SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT',
        value: mockedSettings.createAndPersistTimeout
    },
    {
        label: 'SPAGOBI.WORKMANAGER.SQLDBCACHE.TIMEOUT',
        value: mockedSettings.sqldbCacheTimeout
    },
    {
        label: 'SPAGOBI.CACHE.HAZELCAST.TIMEOUT',
        value: mockedSettings.hazelcastTimeout
    },
    {
        label: 'SPAGOBI.CACHE.HAZELCAST.LEASETIME',
        value: mockedSettings.hazelcastLeaseTime
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() => Promise.resolve({ data: mockedDatasets })),
    put: axios.put.mockImplementation(() => Promise.resolve()),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const factory = (item, datasources, selectedDatasource) => {
    return mount(GeneralSettingsCard, {
        props: {
            item,
            datasources,
            selectedDatasource
        },
        global: {
            plugins: [],
            stubs: { Button, Card, Dropdown, InputNumber, InputText, Toolbar },
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

describe('Cache Management General Settings', () => {
    it('when reset is clicked all values returns to default', async () => {
        const wrapper = factory(mockedSettings, mockedDatasets, mockedDatasets[0])

        wrapper.vm.settings = {
            prefixForCacheTablesName: 'edited',
            spaceAvailable: 512,
            limitForClean: 80,
            schedulingFullClean: { label: 'MONTHLY', value: 'MONTHLY' },
            lastAccessTtl: 300,
            createAndPersistTimeout: 90,
            cacheLimitForStore: 40,
            sqldbCacheTimeout: 12000,
            hazelcastTimeout: 60,
            hazelcastLeaseTime: 180
        }

        await wrapper.find('[data-test="reset-button"]').trigger('click')

        expect(wrapper.vm.selectedDatasource).toStrictEqual(mockedDatasets[0])
    })
    it('when save is clicked the save function is called', async () => {
        const wrapper = factory(mockedSettings, mockedDatasets, mockedDatasets[0])

        expect(wrapper.vm.settings).toStrictEqual(mockedSettings)

        expect(wrapper.find('[data-test="prefix-input"]').wrapperElement._value).toBe('sbicache')
        expect(wrapper.find('[data-test="space-available-input"]').wrapperElement._value).toBe('1024')
        expect(wrapper.find('[data-test="clean-limit-input"]').wrapperElement._value).toBe('90')
        expect(wrapper.find('[data-test="cache-limit-input"]').wrapperElement._value).toBe('10')
        expect(wrapper.find('[data-test="last-access-ttl-input"]').wrapperElement._value).toBe('600')
        expect(wrapper.find('[data-test="sqldb-timeout-input"]').wrapperElement._value).toBe('180000')
        expect(wrapper.find('[data-test="timeout-input"]').wrapperElement._value).toBe('120')
        expect(wrapper.find('[data-test="hazelcast-timeout-input"]').wrapperElement._value).toBe('120')
        expect(wrapper.find('[data-test="hazelcast-lease-time-input"]').wrapperElement._value).toBe('240')

        expect(wrapper.vm.selectedDatasource).toStrictEqual(mockedDatasets[0])
        wrapper.vm.datasource = mockedDatasets[0]

        await wrapper.find('[data-test="save-button"]').trigger('click')
        expect($http.get).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/cacheee/remove')
        expect(axios.put).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasources', { ...mockedDatasets[0], writeDefault: true })

        await flushPromises()

        expect(axios.put).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/configs/conf', { configurations: expectedConfiguration })
        expect(store.setInfo).toHaveBeenCalledTimes(1)
        expect(wrapper.emitted()).toHaveProperty('inserted')
    })
})
