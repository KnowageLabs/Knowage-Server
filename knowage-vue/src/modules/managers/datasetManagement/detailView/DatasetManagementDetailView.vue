<template>
    <!-- MAIN COMPONENT invalid: {{ v$.$invalid }} -->
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ selectedDataset.label }}</template>
        <template #right>
            <Button :label="$t('managers.lovsManagement.preview')" class="p-button-text p-button-rounded p-button-plain" />
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('close')" />
        </template>
    </Toolbar>
    <div class="datasetDetail">
        <TabView class="tabview-custom" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.mondrianSchemasManagement.detail.title') }}</span>
                </template>
                <DetailCard
                    :scopeTypes="scopeTypes"
                    :categoryTypes="categoryTypes"
                    :selectedDataset="selectedDataset"
                    :selectedDatasetVersions="selectedDatasetVersions"
                    :loading="loading"
                    @reloadVersions="getSelectedDatasetVersions"
                    @loadingOlderVersion="$emit('loadingOlderVersion')"
                    @olderVersionLoaded="onOlderVersionLoaded"
                    @touched="$emit('touched')"
                />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.alert.type') }}</span>
                </template>
                <TypeCard
                    :selectedDataset="selectedDataset"
                    :datasetTypes="datasetTypes"
                    :dataSources="dataSources"
                    :businessModels="businessModels"
                    :scriptTypes="scriptTypes"
                    :parentValid="v$.$invalid"
                    :pythonEnvironments="pythonEnvironments"
                    :rEnvironments="rEnvironments"
                    @touched="$emit('touched')"
                />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                </template>
                <MetadataCard :selectedDataset="selectedDataset" @touched="$emit('touched')" />
            </TabPanel>

            <TabPanel v-if="selectedDataset.dsTypeCd == 'Query'">
                <template #header>
                    <span>{{ $t('managers.glossary.glossaryUsage.link') }}</span>
                </template>
                <LinkCard :selectedDataset="selectedDataset" :metaSourceResource="metaSourceResource" @addTables="onAddLinkedTables" @removeTables="onRemoveLinkedTables" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('cron.advanced') }}</span>
                </template>
                <AdvancedCard :selectedDataset="selectedDataset" :transformationDataset="transformationDataset" @touched="$emit('touched')" />
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
import useValidate from '@vuelidate/core'
import { defineComponent } from 'vue'
import axios from 'axios'
import detailViewDescriptor from './DatasetManagementDetailViewDescriptor.json'
import DetailCard from './detailCard/DatasetManagementDetailCard.vue'
import TypeCard from './typeCard/DatasetManagementTypeCard.vue'
import AdvancedCard from './advancedCard/DatasetManagementAdvancedCard.vue'
import LinkCard from './linkCard/DatasetManagementLinkCard.vue'
import MetadataCard from './metadataCard/DatasetManagementMetadataCard.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    components: { TabView, TabPanel, DetailCard, AdvancedCard, LinkCard, TypeCard, MetadataCard },
    props: {
        id: { type: String, required: false },
        scopeTypes: { type: Array as any, required: true },
        categoryTypes: { type: Array as any, required: true },
        datasetTypes: { type: Array as any, required: true },
        transformationDataset: { type: Object as any, required: true },
        scriptTypes: { type: Array as any, required: true },
        dataSources: { type: Array as any, required: true },
        businessModels: { type: Array as any, required: true },
        pythonEnvironments: { type: Array as any, required: true },
        rEnvironments: { type: Array as any, required: true },
        metaSourceResource: { type: Array as any, required: true },
        datasetToCloneId: { type: Number as any }
    },
    computed: {},
    emits: ['close', 'touched', 'loadingOlderVersion', 'olderVersionLoaded'],
    data() {
        return {
            v$: useValidate() as any,
            detailViewDescriptor,
            loading: false,
            touched: false,
            selectedDatasetVersions: [] as any,
            tablesToAdd: [] as any,
            tablesToRemove: [] as any,
            selectedDataset: {} as any,
            loadingVersion: false
        }
    },
    created() {
        this.id ? this.getAllDatasetData() : ''
    },
    watch: {
        id() {
            this.getAllDatasetData()
        },
        datasetToCloneId() {
            this.cloneDatasetConfirm(this.datasetToCloneId)
        }
    },
    validations() {},
    methods: {
        async getSelectedDataset() {
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.id}`)
                .then((response) => {
                    this.selectedDataset = response.data[0] ? { ...response.data[0] } : {}
                })
                .catch()
        },
        async getSelectedDatasetVersions() {
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/olderversions/${this.id}`)
                .then((response) => {
                    response.data.root ? (this.selectedDatasetVersions = response.data.root) : (this.selectedDatasetVersions = [])
                })
                .catch()
                .finally(() => (this.loading = false))
        },
        async getAllDatasetData() {
            this.loading = true
            await this.getSelectedDataset()
            await this.getSelectedDatasetVersions()
        },
        cloneDatasetConfirm(datasetId) {
            this.$confirm.require({
                icon: 'pi pi-exclamation-triangle',
                message: this.$t('kpi.kpiDefinition.confirmClone'),
                header: this.$t(' '),
                datasetId,
                accept: () => this.cloneDataset(datasetId)
            })
        },
        async cloneDataset(datasetId) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${datasetId}`).then((response) => {
                delete response.data[0].id
                response.data[0].label = '...'
                response.data[0].dsVersions = []
                response.data[0].usedByNDocs = 0

                this.selectedDataset = { ...response.data[0] }
            })
        },
        onAddLinkedTables(event) {
            this.tablesToAdd = event
            this.$emit('touched')
        },
        onRemoveLinkedTables(event) {
            this.tablesToRemove = event
            this.$emit('touched')
        },
        onOlderVersionLoaded(event) {
            this.$emit('olderVersionLoaded')
            this.selectedDataset = { ...event }
        }
    }
})
</script>
<style lang="scss" scoped>
.datasetDetail {
    overflow: auto;
    flex: 1;
    display: flex;
    flex-direction: column;
}
</style>
