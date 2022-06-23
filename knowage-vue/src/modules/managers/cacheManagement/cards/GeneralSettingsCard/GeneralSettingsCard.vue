<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-toolbar-group-right">
                <template #start>
                    {{ $t('managers.cacheManagement.generalSettings') }}
                </template>
                <template #end>
                    <Button class="kn-button p-button-text p-button-rounded" @click="save" data-test="save-button">{{ $t('common.save') }}</Button>
                    <Button class="kn-button p-button-text p-button-rounded" @click="discardChanges" data-test="reset-button">{{ $t('managers.cacheManagement.discard') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <form class="p-fluid p-m-2">
                <div class="p-fluid p-formgrid p-grid">
                    <div class="p-col-6">
                        <div id="prefix-input-container" class="p-field">
                            <span class="p-float-label field-container">
                                <InputText id="prefixForCacheTablesName" class="kn-material-input" type="text" v-model.trim="settings.prefixForCacheTablesName" data-test="prefix-input" />
                                <label for="prefixForCacheTablesName" class="kn-material-input-label"> {{ $t('managers.cacheManagement.prefixForCacheTablesName') }}</label>
                            </span>
                        </div>
                        <div class="p-field">
                            <span>
                                <label for="limitForClean" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.maximumPercentOfCacheCleaningQuota') }}</label>
                                <InputNumber id="limitForClean" inputClass="kn-material-input" v-model="settings.limitForClean" :min="0" :max="100" :useGrouping="false" @input="onInputNumberChange('limitForClean', $event.value)" data-test="clean-limit-input" />
                            </span>
                        </div>
                        <div class="p-field">
                            <span>
                                <label for="schedulingFullClean" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.frequencyOfCleaningDaemon') }}</label>
                                <Dropdown id="schedulingFullClean" class="kn-material-input" v-model="settings.schedulingFullClean" :options="generalSettingsCardDescriptor.schedulingTypes">
                                    <template #value="slotProps">
                                        <div v-if="slotProps.value">
                                            <span>{{ slotProps.value.value }}</span>
                                        </div>
                                    </template>
                                    <template #option="slotProps">
                                        <div>
                                            <span>{{ $t(slotProps.option.label) }}</span>
                                        </div>
                                    </template></Dropdown
                                >
                            </span>
                        </div>
                        <div class="p-field">
                            <span>
                                <label for="lastAccessTtl" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.ttlForCachedDataset') }}</label>
                                <InputNumber id="lastAccessTtl" inputClass="kn-material-input" v-model="settings.lastAccessTtl" :useGrouping="false" @input="onInputNumberChange('lastAccessTtl', $event.value)" data-test="last-access-ttl-input" />
                            </span>
                        </div>
                        <div class="p-field">
                            <span>
                                <label for="createAndPersistTimeout" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.timeToCreateTempTable') }}</label>
                                <InputNumber id="createAndPersistTimeout" inputClass="kn-material-input" v-model="settings.createAndPersistTimeout" :useGrouping="false" @input="onInputNumberChange('createAndPersistTimeout', $event.value)" data-test="timeout-input" />
                            </span>
                        </div>
                    </div>
                    <div class="p-col-6">
                        <div class="p-field">
                            <span>
                                <label for="spaceAvailable" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.totalBytesAvailableForCache') }}</label>
                                <InputNumber id="spaceAvailable" inputClass="kn-material-input" v-model="settings.spaceAvailable" :min="0" :useGrouping="false" @input="onInputNumberChange('spaceAvailable', $event.value)" data-test="space-available-input" />
                                <small id="spaceAvailable-hint">{{ formatCacheDimension(settings.spaceAvailable) }}</small>
                            </span>
                        </div>
                        <div class="p-field">
                            <span>
                                <label for="cacheLimitForStore" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.cacheDimensionSingleDataset') }}</label>
                                <InputNumber id="cacheLimitForStore" inputClass="kn-material-input" v-model="settings.cacheLimitForStore" :min="0" :max="100" :useGrouping="false" @input="onInputNumberChange('cacheLimitForStore', $event.value)" data-test="cache-limit-input" />
                            </span>
                        </div>
                        <div class="p-field">
                            <span>
                                <label for="datasource" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.targetDatasource') }}</label>
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
                        </div>
                        <div class="p-field">
                            <span>
                                <label for="sqldbCacheTimeout" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.timeToLock') }}</label>
                                <InputNumber id="sqldbCacheTimeout" inputClass="kn-material-input" v-model="settings.sqldbCacheTimeout" :useGrouping="false" @input="onInputNumberChange('sqldbCacheTimeout', $event.value)" data-test="sqldb-timeout-input" />
                            </span>
                        </div>
                        <div class="p-field">
                            <span>
                                <label for="hazelcastTimeout" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.hazelcastTimeToLock') }}</label>
                                <InputNumber id="hazelcastTimeout" inputClass="kn-material-input" v-model="settings.hazelcastTimeout" :useGrouping="false" @input="onInputNumberChange('hazelcastTimeout', $event.value)" data-test="hazelcast-timeout-input" />
                            </span>
                        </div>
                        <div class="p-field">
                            <span>
                                <label for="hazelcastLeaseTime" class="kn-material-input-label small-label"> {{ $t('managers.cacheManagement.hazelcastTimeToReleaseLock') }}</label>
                                <InputNumber id="hazelcastLeaseTime" inputClass="kn-material-input" v-model="settings.hazelcastLeaseTime" :useGrouping="false" @input="onInputNumberChange('hazelcastLeaseTime', $event.value)" data-test="hazelcast-lease-time-input" />
                            </span>
                        </div>
                    </div>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iSettings } from '../../CacheManagement'
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
            type: Object,
            required: true
        },
        datasources: {
            type: Array,
            required: true
        },
        selectedDatasource: {
            value: [Object, null],
            required: true
        }
    },
    emits: ['inserted'],
    data() {
        return {
            generalSettingsCardDescriptor,
            settings: {} as iSettings,
            datasource: {},
            datasourceOptions: []
        }
    },
    watch: {
        item() {
            this.loadSettings()
        },
        datasources() {
            this.loadDatasources()
        },
        selectedDatasource() {
            this.loadDatasource()
        }
    },
    created() {
        this.loadSettings()
        this.loadDatasources()
        this.loadDatasource()
    },
    methods: {
        loadSettings() {
            this.settings = { ...this.item } as iSettings
        },
        loadDatasources() {
            this.datasourceOptions = this.datasources as []
        },
        loadDatasource() {
            this.datasource = { ...(this.selectedDatasource as Object) }
        },
        async save() {
            await this.removeCache()
            await this.saveDatasource()
            await this.saveConfigurationOptions()

            this.store.commit('setInfo', {
                title: this.$t('common.toast.success'),
                msg: this.$t('common.toast.updateTitle')
            })
            this.$emit('inserted')
        },
        async saveConfigurationOptions() {
            const configurations = [
                { label: 'SPAGOBI.CACHE.NAMEPREFIX', value: this.settings.prefixForCacheTablesName },
                { label: 'SPAGOBI.CACHE.SPACE_AVAILABLE', value: this.settings.spaceAvailable * 1048576 },
                { label: 'SPAGOBI.CACHE.LIMIT_FOR_CLEAN', value: this.settings.limitForClean },
                { label: 'SPAGOBI.CACHE.SCHEDULING_FULL_CLEAN', value: this.settings.schedulingFullClean.value, id: this.settings.schedulingFullClean.value },
                { label: 'SPAGOBI.CACHE.LIMIT_FOR_STORE', value: this.settings.cacheLimitForStore },
                { label: 'SPAGOBI.CACHE.DS_LAST_ACCESS_TTL', value: this.settings.lastAccessTtl },
                { label: 'SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT', value: this.settings.createAndPersistTimeout },
                { label: 'SPAGOBI.WORKMANAGER.SQLDBCACHE.TIMEOUT', value: this.settings.sqldbCacheTimeout },
                { label: 'SPAGOBI.CACHE.HAZELCAST.TIMEOUT', value: this.settings.hazelcastTimeout },
                { label: 'SPAGOBI.CACHE.HAZELCAST.LEASETIME', value: this.settings.hazelcastLeaseTime }
            ]

            await this.$http
                .put(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/configs/conf', { configurations: configurations })
                .then()
                .catch((error) => {
                    console.log(error)
                })
        },
        async saveDatasource() {
            await this.$http.put(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasources', { ...this.datasource, writeDefault: true })
        },
        async removeCache() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/cacheee/remove')
        },
        onInputNumberChange(field: string, value: number) {
            this.settings[field] = value
        },
        discardChanges() {
            this.loadSettings()
            this.loadDatasource()
        },
        formatCacheDimension(size: number) {
            if (isNaN(size)) return 0
            if (size < 1024) return size.toFixed(2) + ' MB'
            size /= 1024
            if (size < 1024) return '~' + size.toFixed(2) + ' GB'
            size /= 1024
            return '~' + size.toFixed(2) + ' TB'
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}

#prefix-input-container {
    margin-top: 1.2rem;
    margin-bottom: 2.2rem;
}

.small-label {
    font-size: 0.9rem;
}

#spaceAvailable-hint {
    font-size: 0.6rem;
}
</style>
