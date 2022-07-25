<template>
    <Teleport to=".dashboard-container">
        <div class="datasetEditor">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ $t('dashboard.datasetEditor.title') }} </template>
                <template #end>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('closeDatasetEditor')" />
                </template>
            </Toolbar>
            <div v-if="!loading" class="datasetEditor-container">
                <div class="datasetEditor-tabs">
                    <TabView>
                        <TabPanel :header="$t('dashboard.datasetEditor.dataTabTitle')">
                            <DataTab :dashboardDatasetsProp="dashboardDatasets" :availableDatasetsProp="availableDatasets" :selectedDatasetsProp="selectedDatasets" @addSelectedDatasets="addSelectedDatasets" />
                            <DatasetEditorPreview v-if="!loading" :dashboardDatasetsProp="dashboardDatasets" />
                        </TabPanel>
                        <TabPanel :header="$t('dashboard.datasetEditor.associationsTabTitle')">
                            <AssociationsTab :dashboardAssociationsProp="dashboardAssociations" :selectedDatasetsProp="selectedDatasets" />
                        </TabPanel>
                    </TabView>
                </div>
            </div>
        </div>
    </Teleport>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the dataset.
 */
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import DatasetEditorPreview from './DatasetEditorPreview.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DataTab from './DatasetEditorDataTab/DatasetEditorDataTab.vue'
import AssociationsTab from './DatasetEditorAssociations/DatasetEditorAssociations.vue'
import mainStore from '../../../../App.store'
import dashStore from '../Dashboard.store'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'dataset-editor',
    components: { TabView, TabPanel, DataTab, AssociationsTab, DatasetEditorPreview },
    props: {},
    emits: ['closeDatasetEditor'],
    data() {
        return {
            loading: false,
            dashboardDatasets: {} as any,
            dashboardAssociations: {} as any,
            availableDatasets: {} as any,
            selectedDatasets: [] as any
        }
    },
    setup() {
        const store = mainStore()
        const dashboardStore = dashStore()
        return { store, dashboardStore }
    },
    created() {
        console.log('STORE MODEL', this.dashboardStore.$state.dashboards[1])
        this.dashboardDatasets = deepcopy(this.dashboardStore.$state.dashboards[1].configuration.datasets)
        this.dashboardAssociations = deepcopy(this.dashboardStore.$state.dashboards[1].configuration.associations)
        this.getDatasets()
    },

    methods: {
        async getDatasets() {
            this.store.setLoading(true)
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/?asPagedList=true&seeTechnical=true`)
                .then((response: AxiosResponse<any>) => {
                    this.availableDatasets = response.data.item
                    this.selectedDatasets = this.filterSelectedFromAvailableDatasets()
                })
                .finally(() => {
                    this.store.setLoading(false)
                    this.loading = false
                })
        },
        filterSelectedFromAvailableDatasets() {
            return this.availableDatasets?.filter((responseDataset) => {
                return this.dashboardDatasets?.find((dashboardDataset) => {
                    return responseDataset.id.dsId === dashboardDataset.id
                })
            })
        },
        addSelectedDatasets(datasetsToAdd) {
            datasetsToAdd.forEach((dataset) => {
                this.selectedDatasets.push(dataset)
            })
            console.log('dataset Added -------', this.selectedDatasets)
        }
    }
})
</script>
<style lang="scss">
.datasetEditor {
    height: 100vh;
    width: 100%;
    top: 0;
    left: 0;
    background-color: white;
    position: absolute;
    z-index: 999;
    display: flex;
    flex-direction: column;
    .datasetEditor-container {
        flex: 1;
        display: flex;
    }
}
.datasetEditor-tabs {
    flex: 1;
    .p-tabview {
        width: 100%;
        height: 100%;
        .p-tabview-panels {
            padding: 0;
            display: flex;
            flex-direction: column;
            height: calc(100% - 36px);
            .p-tabview-panel {
                display: flex;
                flex: 1;
            }
        }
    }
}
</style>
