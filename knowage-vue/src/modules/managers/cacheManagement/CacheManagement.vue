<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                CACHE MANAGEMENT
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
        <div class="kn-page-content p-grid p-m-2">
            <div class="p-col-6 p-sm-12 p-md-6 p-p-0">
                <RuntimeInformationCard :item="cache" :chartData="chartData"></RuntimeInformationCard>
            </div>
            <div class="p-col-6 p-sm-12 p-md-6 p-p-0">
                <GeneralSettingsCard :item="settings" :datasources="datasources" :selectedDatasource="selectedDatasource"></GeneralSettingsCard>
            </div>
            <div class="p-col-12 p-sm-12 p-p-0">
                TABLE
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iCache } from './CacheManagement'
import axios from 'axios'
import GeneralSettingsCard from './cards/GeneralSettingsCard/GeneralSettingsCard.vue'
import RuntimeInformationCard from './cards/RuntimeInformationCard/RuntimeInformationCard.vue'

export default defineComponent({
    name: 'cache-management',
    components: {
        GeneralSettingsCard,
        RuntimeInformationCard
    },
    data() {
        return {
            cache: {} as iCache,
            loading: false,
            chartData: [] as any,
            settings: {} as any,
            selectedDatasource: null as any,
            datasources: [] as any
        }
    },
    async created() {
        await this.loadCache()
        await this.loadDataSources()
        console.log('Datasources: ', this.datasources)
        // TODO pitati za ovo
        if (this.selectedDatasource === null) {
            this.$store.commit('setInfo', {
                title: 'TITLE',
                msg: 'No default datasource set, cannot display cache runtime information!'
            })
        }
        await this.loadSettings()
        console.log('Settings: ', this.settings)
    },
    methods: {
        async loadCache() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/cacheee')
                .then((response) => {
                    this.cache = response.data
                    this.chartData = [this.cache.availableMemoryPercentage, 100 - this.cache.availableMemoryPercentage]
                })
                .finally(() => (this.loading = false))
        },
        async loadSettings() {
            this.loading = true
            const tempSettings = {} as any
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.NAMEPREFIX').then((response) => (tempSettings.prefixForCacheTablesName = response.data.valueCheck))
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.LIMIT_FOR_CLEAN').then((response) => (tempSettings.limitForClean = +response.data.valueCheck))
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.SCHEDULING_FULL_CLEAN').then((response) => (tempSettings.schedulingFullClean = { label: response.data.valueCheck, value: response.data.valueCheck }))
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.DS_LAST_ACCESS_TTL').then((response) => (tempSettings.lastAccessTtl = +response.data.valueCheck))
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT').then((response) => (tempSettings.createAndPersistTimeout = +response.data.valueCheck))
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.SPACE_AVAILABLE').then((response) => (tempSettings.spaceAvailable = +response.data.valueCheck / 1048576))
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.LIMIT_FOR_STORE').then((response) => (tempSettings.cacheLimitForStore = +response.data.valueCheck))
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.WORKMANAGER.SQLDBCACHE.TIMEOUT').then((response) => (tempSettings.sqldbCacheTimeout = +response.data.valueCheck))
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.HAZELCAST.TIMEOUT').then((response) => (tempSettings.hazelcastTimeout = +response.data.valueCheck))
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs/label/SPAGOBI.CACHE.HAZELCAST.LEASETIME').then((response) => (tempSettings.hazelcastLeaseTime = +response.data.valueCheck))
            this.settings = { ...tempSettings }
            this.loading = false
        },
        async loadDataSources() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasources/?type=cache').then((response) => {
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
        }
    }
})
</script>
