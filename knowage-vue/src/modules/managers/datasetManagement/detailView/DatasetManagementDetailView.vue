<template>
    <!-- MAIN COMPONENT invalid: {{ v$.$invalid }} -->
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ datasetInList.label }}</template>
        <template #right>
            <Button label="PREVIEW" class="p-button-text p-button-rounded p-button-plain" />
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
                <DetailCard :scopeTypes="scopeTypes" :categoryTypes="categoryTypes" :selectedDataset="selectedDataset" :selectedDatasetVersions="selectedDatasetVersions" :loading="loading" @reloadVersions="getSelectedDatasetVersions" @touched="$emit('touched')" />
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
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    components: { TabView, TabPanel, DetailCard, AdvancedCard, LinkCard, TypeCard },
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
    emits: ['close', 'touched'],
    data() {
        return {
            v$: useValidate() as any,
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

        onAddLinkedTables(event) {
            this.tablesToAdd = event
            this.$emit('touched')
        },
        onRemoveLinkedTables(event) {
            this.tablesToRemove = event
            this.$emit('touched')
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
