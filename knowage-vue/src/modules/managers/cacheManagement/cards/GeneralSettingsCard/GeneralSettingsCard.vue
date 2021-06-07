<template>
    <Card class="p-m-3">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.cacheManagement.generalSettings') }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text p-button-rounded" @click="save">{{ $t('common.save') }}</Button>
                    <Button class="kn-button p-button-text p-button-rounded" @click="test">{{ $t('managers.cacheManagement.discard') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <form class="p-fluid p-m-2">
                <div class="p-fluid p-formgrid p-grid">
                    <div class="p-field p-col">
                        <span class="p-float-label">
                            <InputText id="prefixForCacheTablesName" class="kn-material-input" type="text" v-model.trim="settings.prefixForCacheTablesName" data-test="prefix-input" />
                            <label for="prefixForCacheTablesName" class="kn-material-input-label"> {{ $t('managers.cacheManagement.prefixForCacheTablesName') }}</label>
                        </span>
                        <span>
                            <label for="limitForClean" class="kn-material-input-label"> {{ $t('managers.cacheManagement.maximumPercentOfCacheCleaningQuota') }}</label>
                            <InputNumber id="limitForClean" inputClass="kn-material-input" v-model.trim="settings.limitForClean" :min="0" :max="100" :useGrouping="false" data-test="clean-limit-input" />
                        </span>
                        <span>
                            <label for="schedulingFullClean" class="kn-material-input-label"> {{ $t('managers.cacheManagement.frequencyOfCleaningDaemon') }}</label>
                            <Dropdown id="schedulingFullClean" class="kn-material-input" v-model="settings.schedulingFullClean" :options="generalSettingsCardDescriptor.schedulingTypes">
                                <template #value="slotProps">
                                    <div v-if="slotProps.value">
                                        <span>{{ slotProps.value.value }}</span>
                                    </div>
                                </template>
                                <template #option="slotProps">
                                    <div>
                                        <!-- <span>{{ $t(slotProps.option.label) }}</span> -->
                                        <span>{{ $t(slotProps.option.label) }}</span>
                                    </div>
                                </template></Dropdown
                            >
                        </span>
                        <span>
                            <label for="lastAccessTtl" class="kn-material-input-label"> {{ $t('managers.cacheManagement.ttlForCachedDataset') }}</label>
                            <InputNumber id="lastAccessTtl" inputClass="kn-material-input" v-model.trim="settings.lastAccessTtl" :useGrouping="false" data-test="last-access-ttl-input" />
                        </span>
                        <span>
                            <label for="createAndPersistTimeout" class="kn-material-input-label"> {{ $t('managers.cacheManagement.timeToCreateTempTable') }}</label>
                            <InputNumber id="createAndPersistTimeout" inputClass="kn-material-input" v-model.trim="settings.createAndPersistTimeout" :useGrouping="false" data-test="timeout-input" />
                        </span>
                    </div>

                    <div class="p-field p-col">
                        <span>
                            <label for="spaceAvailable" class="kn-material-input-label"> {{ $t('managers.cacheManagement.totalBytesAvailableForCache') }}</label>
                            <InputNumber id="spaceAvailable" inputClass="kn-material-input" v-model.trim="settings.spaceAvailable" :min="0" :useGrouping="false" data-test="space-available-input" />
                        </span>
                        <span>
                            <label for="cacheLimitForStore" class="kn-material-input-label"> {{ $t('managers.cacheManagement.cacheDimensionSingleDataset') }}</label>
                            <InputNumber id="cacheLimitForStore" inputClass="kn-material-input" v-model.trim="settings.cacheLimitForStore" :min="0" :max="100" :useGrouping="false" data-test="cache-limit-input" />
                        </span>
                        <span>
                            <label for="datasource" class="kn-material-input-label"> {{ $t('managers.cacheManagement.frequencyOfCleaningDaemon') }}</label>
                            <Dropdown id="datasource" class="kn-material-input" v-model="datasource" :options="datasourceOptions">
                                <template #value="slotProps">
                                    <div v-if="slotProps.value">
                                        <span>{{ slotProps.value.label }}</span>
                                    </div>
                                </template>
                                <template #option="slotProps">
                                    <div>
                                        <span>{{ slotProps.option.label }}</span>
                                    </div>
                                </template></Dropdown
                            >
                        </span>
                        <span>
                            <label for="sqldbCacheTimeout" class="kn-material-input-label"> {{ $t('managers.cacheManagement.timeToLock') }}</label>
                            <InputNumber id="sqldbCacheTimeout" inputClass="kn-material-input" v-model.trim="settings.sqldbCacheTimeout" :useGrouping="false" data-test="sqldb-timeout-input" />
                        </span>
                        <span>
                            <label for="hazelcastTimeout" class="kn-material-input-label"> {{ $t('managers.cacheManagement.hazelcastTimeToLock') }}</label>
                            <InputNumber id="hazelcastTimeout" inputClass="kn-material-input" v-model.trim="settings.hazelcastTimeout" :useGrouping="false" data-test="hazelcast-timeout-input" />
                        </span>
                        <span>
                            <label for="hazelcastLeaseTime" class="kn-material-input-label"> {{ $t('managers.cacheManagement.hazelcastTimeToReleaseLock') }}</label>
                            <InputNumber id="hazelcastLeaseTime" inputClass="kn-material-input" v-model.trim="settings.hazelcastLeaseTime" :useGrouping="false" data-test="hazelcast-lease-time-input" />
                        </span>
                    </div>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import generalSettingsCardDescriptor from './GeneralSettingsCardDescriptor.json'

export default defineComponent({
    name: 'general-settings-card',
    components: {
        Dropdown,
        InputNumber
    },
    props: {
        item: {
            type: Object
        },
        datasources: {
            type: Array
        },
        selectedDatasource: {
            type: Object
        }
    },
    data() {
        return {
            generalSettingsCardDescriptor,
            settings: {} as any,
            datasource: {},
            datasourceOptions: [] as any
        }
    },
    watch: {
        item() {
            this.loadSettings()
        },
        datasources() {
            this.loadDatasources()
            console.log('datasource options', this.datasourceOptions)
        },
        selectedDatasource() {
            this.loadDatasource()
            console.log('selected datasource', this.datasource)
        }
    },
    created() {
        this.loadSettings()
    },
    methods: {
        loadSettings() {
            this.settings = { ...this.item }
        },
        loadDatasources() {
            this.datasourceOptions = this.datasources as any
        },
        loadDatasource() {
            this.datasource = { ...this.selectedDatasource }
        },
        save() {
            this.saveConfigurationOptions()
        },
        saveConfigurationOptions() {
            const configurations = [
                {
                    label: 'SPAGOBI.CACHE.NAMEPREFIX',
                    value: this.settings.prefixForCacheTablesName
                },
                {
                    label: 'SPAGOBI.CACHE.SPACE_AVAILABLE',
                    value: this.settings.spaceAvailable
                },
                {
                    label: 'SPAGOBI.CACHE.LIMIT_FOR_CLEAN',
                    value: this.settings.limitForClean
                },
                {
                    label: 'SPAGOBI.CACHE.SCHEDULING_FULL_CLEAN',
                    value: this.settings.schedulingFullClean,
                    id: ''
                },
                {
                    label: 'SPAGOBI.CACHE.LIMIT_FOR_STORE',
                    value: this.settings.cacheLimitForStore
                },
                {
                    label: 'SPAGOBI.CACHE.DS_LAST_ACCESS_TTL',
                    value: this.settings.lastAccessTtl
                },
                {
                    label: 'SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT',
                    value: this.settings.createAndPersistTimeout
                },
                {
                    label: 'SPAGOBI.WORKMANAGER.SQLDBCACHE.TIMEOUT',
                    value: this.settings.sqldbCacheTimeout
                },
                {
                    label: 'SPAGOBI.CACHE.HAZELCAST.TIMEOUT',
                    value: this.settings.hazelcastTimeout
                },
                {
                    label: 'SPAGOBI.CACHE.HAZELCAST.LEASETIME',
                    value: this.settings.hazelcastLeaseTime
                }
            ]

            console.log(configurations)
        },
        test() {
            console.log(this.settings)
        }
    }
})
</script>
