<template>
    <div class="cache-management kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('managers.cacheManagement.title') }}
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="showProgressBar" data-test="progress-bar" />
        <div class="p-d-flex p-flex-wrap kn-page-content">
            <div class="p-col-4 p-sm-12 p-md-4 p-p-0">
                <RuntimeInformationCard v-if="selectedDatasource" :item="cache" :chartData="chartData"></RuntimeInformationCard>
            </div>
            <div class="p-col-8 p-sm-12 p-md-8 p-p-0">
                <GeneralSettingsCard v-if="settingsPendingCount == 0" :item="settings" :datasources="datasources" :selectedDatasource="selectedDatasource" @inserted="pageReload"></GeneralSettingsCard>
            </div>
            <div class="p-col-12 p-sm-12 p-p-0">
                <DatasetTableCard :datasetMetadataList="datasetMetadataList" :loading="datasetMetadataLoading" @deleted="loadDatasetsMetadata"></DatasetTableCard>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iCache, iMeta, iSettings } from './CacheManagement'
import axios from 'axios'
import DatasetTableCard from './cards/DatasetTableCard/DatasetTableCard.vue'
import GeneralSettingsCard from './cards/GeneralSettingsCard/GeneralSettingsCard.vue'
import RuntimeInformationCard from './cards/RuntimeInformationCard/RuntimeInformationCard.vue'

export default defineComponent({
    name: 'cache-management',
    components: {
        DatasetTableCard,
        GeneralSettingsCard,
        RuntimeInformationCard
    },
    data() {
        return {
            cache: {} as iCache,
            loading: false,
            datasetMetadataLoading: true,
            settingsPendingCount: 10,
            chartData: [] as any,
            datasetMetadataList: [] as iMeta[],
            settings: {} as iSettings,
            selectedDatasource: null as any,
            datasources: [] as any
        }
    },
    computed: {
        showProgressBar(): boolean {
            return this.loading || this.settingsPendingCount != 0
        }
    },
    async created() {
        this.loadPage()
    },
    methods: {
        loadCache() {
            axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/cacheee').then((response) => {
                this.cache = response.data
                this.chartData = [this.cache.availableMemoryPercentage, 100 - this.cache.availableMemoryPercentage]
            })
        },
        loadSettings() {
            this.settings = {} as iSettings
            this.settingsPendingCount = 10
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.NAMEPREFIX')
                .then((response) => (this.settings.prefixForCacheTablesName = response.data.valueCheck))
                .finally(() => this.settingsPendingCount--)
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.LIMIT_FOR_CLEAN')
                .then((response) => (this.settings.limitForClean = +response.data.valueCheck))
                .finally(() => this.settingsPendingCount--)
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.SCHEDULING_FULL_CLEAN')
                .then((response) => (this.settings.schedulingFullClean = { label: response.data.valueCheck, value: response.data.valueCheck }))
                .finally(() => this.settingsPendingCount--)
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.DS_LAST_ACCESS_TTL')
                .then((response) => (this.settings.lastAccessTtl = +response.data.valueCheck))
                .finally(() => this.settingsPendingCount--)
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT')
                .then((response) => (this.settings.createAndPersistTimeout = +response.data.valueCheck))
                .finally(() => this.settingsPendingCount--)
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.SPACE_AVAILABLE')
                .then((response) => (this.settings.spaceAvailable = +response.data.valueCheck / 1048576))
                .finally(() => this.settingsPendingCount--)
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.LIMIT_FOR_STORE')
                .then((response) => (this.settings.cacheLimitForStore = +response.data.valueCheck))
                .finally(() => this.settingsPendingCount--)
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.WORKMANAGER.SQLDBCACHE.TIMEOUT')
                .then((response) => (this.settings.sqldbCacheTimeout = +response.data.valueCheck))
                .finally(() => this.settingsPendingCount--)
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.HAZELCAST.TIMEOUT')
                .then((response) => (this.settings.hazelcastTimeout = +response.data.valueCheck))
                .finally(() => this.settingsPendingCount--)
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.HAZELCAST.LEASETIME')
                .then((response) => (this.settings.hazelcastLeaseTime = +response.data.valueCheck))
                .finally(() => this.settingsPendingCount--)
        },
        async loadDataSources() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasources/?type=cache')
                .then((response) => {
                    this.datasources = []
                    response.data.map((datasource: any) => {
                        if (datasource.readOnly === false) {
                            this.datasources.push(datasource)
                        }
                        if (datasource.writeDefault === true) {
                            this.selectedDatasource = datasource
                        }
                    })
                })
                .finally(() => (this.loading = false))

            if (this.selectedDatasource === null) {
                this.$store.commit('setError', {
                    title: this.$t('managers.cacheManagement.noDefaultDatasetTitle'),
                    msg: this.$t('managers.cacheManagement.noDefaultDataset')
                })
            }
        },
        loadDatasetsMetadata() {
            this.datasetMetadataLoading = true
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/cacheee/meta')
                .then((response) => (this.datasetMetadataList = response.data))
                .finally(() => (this.datasetMetadataLoading = false))
        },
        async loadPage() {
            await this.loadDataSources()
            this.loadCache()
            this.loadSettings()
            this.loadDatasetsMetadata()
        },
        pageReload() {
            this.loadPage()
        }
    }
})
</script>
