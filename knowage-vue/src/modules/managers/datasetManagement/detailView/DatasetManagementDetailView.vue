<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ datasetInList.label }}</template>
        <template #right>
            <Button label="PREVIEW" class="p-button-text p-button-rounded p-button-plain" />
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeDetailConfirm" />
        </template>
    </Toolbar>

    <div class="p-d-flex p-flex-row">
        <div class="card kn-flex">
            <TabView class="tabview-custom" data-test="tab-view">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('managers.mondrianSchemasManagement.detail.title') }}</span>
                    </template>
                    <DetailCard :scopeTypes="scopeTypes" :categoryTypes="categoryTypes" :selectedDataset="selectedDataset" :selectedDatasetVersions="selectedDatasetVersions" :loading="loading" @reloadVersions="getSelectedDatasetVersions" />
                </TabPanel>

                <TabPanel>
                    <template #header>
                        <span>{{ $t('kpi.alert.type') }}</span>
                    </template>
                </TabPanel>

                <TabPanel>
                    <template #header>
                        <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                    </template>
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
                    <AdvancedCard :selectedDataset="selectedDataset" :transformationDataset="transformationDataset" />
                </TabPanel>
            </TabView>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import detailViewDescriptor from './DatasetManagementDetailViewDescriptor.json'
import DetailCard from './detailCard/DatasetManagementDetailCard.vue'
import AdvancedCard from './advancedCard/DatasetManagementAdvancedCard.vue'
import LinkCard from './linkCard/DatasetManagementLinkCard.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    components: { TabView, TabPanel, DetailCard, AdvancedCard, LinkCard },
    props: {
        id: { type: String, required: false },
        datasetInList: {} as any,
        scopeTypes: { type: Array as any, required: true },
        categoryTypes: { type: Array as any, required: true },
        datasetTypes: { type: Array as any, required: true },
        transformationDataset: { type: Object as any, required: true },
        scriptTypes: { type: Array as any, required: true },
        dataSources: { type: Array as any, required: true },
        businessModels: { type: Array as any, required: true },
        pythonEnvironments: { type: Array as any, required: true },
        rEnvironments: { type: Array as any, required: true },
        metaSourceResource: { type: Array as any, required: true }
    },
    computed: {},
    emits: ['close'],
    data() {
        return {
            detailViewDescriptor,
            loading: false,
            touched: false,
            selectedDatasetVersions: [] as any,
            tablesToAdd: [] as any,
            tablesToRemove: [] as any,
            selectedDataset: {} as any
        }
    },
    created() {
        this.id ? this.getAllDatasetData() : ''
    },
    watch: {
        id() {
            this.getAllDatasetData()
        }
    },
    validations() {},
    methods: {
        //#region ===================== Get All Data and Format ====================================================
        async getSelectedDataset() {
            axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.id}`).then((response) => {
                this.selectedDataset = response.data[0] ? { ...response.data[0] } : {}
            })
        },
        async getSelectedDatasetVersions() {
            axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/olderversions/${this.id}`)
                .then((response) => {
                    response.data.root ? (this.selectedDatasetVersions = response.data.root) : (this.selectedDatasetVersions = [])
                })
                .finally(() => (this.loading = false))
        },
        async getAllDatasetData() {
            this.loading = true
            await this.getSelectedDataset()
            await this.getSelectedDatasetVersions()
        },
        //#endregion ================================================================================================

        closeDetailConfirm() {
            if (!this.touched) {
                this.$emit('close')
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push('/dataset-management')
                        this.$emit('close')
                    }
                })
            }
        },
        onAddLinkedTables(event) {
            this.tablesToAdd = event
            this.touched = true
        },
        onRemoveLinkedTables(event) {
            this.tablesToRemove = event
            this.touched = true
        }
    }
})
</script>
