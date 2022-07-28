<template>
    <Teleport to=".dashboard-container">
        <div class="datasetEditor">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ $t('dashboard.datasetEditor.title') }} </template>
                <template #end>
                    <Button :disabled="modelHasEmptyAssociations" icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('closeDatasetEditor')" />
                </template>
            </Toolbar>
            {{ dashboardAssociations }}
            <TabView v-if="!loading" class="datasetEditor-tabs">
                <TabPanel :header="$t('dashboard.datasetEditor.dataTabTitle')">
                    <DataTab :dashboardDatasetsProp="dashboardDatasets" :availableDatasetsProp="availableDatasets" :selectedDatasetsProp="selectedDatasets" @addSelectedDatasets="addSelectedDatasets" />
                </TabPanel>
                <!-- <TabPanel :header="$t('dashboard.datasetEditor.associationsTabTitle')"> -->
                <TabPanel>
                    <template #header>
                        <span v-bind:class="{ 'details-warning-color': modelHasEmptyAssociations }">{{ $t('dashboard.datasetEditor.associationsTabTitle') }}</span>
                        <i v-if="modelHasEmptyAssociations" class="fa-solid fa-circle-exclamation p-ml-1" v-bind:class="{ 'details-warning-color': modelHasEmptyAssociations }" />
                    </template>
                    <AssociationsTab :dashboardAssociationsProp="dashboardAssociations" :selectedDatasetsProp="selectedDatasets" :selectedAssociationProp="selectedAssociation" @createNewAssociation="createNewAssociation" @associationDeleted="deleteAssociation" />
                </TabPanel>
            </TabView>
        </div>
    </Teleport>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the dataset.
 */
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { IAssociation } from '../Dashboard'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DataTab from './DatasetEditorDataTab/DatasetEditorDataTab.vue'
import AssociationsTab from './DatasetEditorAssociations/DatasetEditorAssociations.vue'
import mainStore from '../../../../App.store'
import dashStore from '../Dashboard.store'
import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'dataset-editor',
    components: { TabView, TabPanel, DataTab, AssociationsTab },
    props: {},
    emits: ['closeDatasetEditor'],
    data() {
        return {
            loading: false,
            availableDatasets: {} as any,
            dashboardDatasets: {} as any,
            selectedDatasets: [] as any,
            dashboardAssociations: [] as IAssociation[],
            selectedAssociation: {} as IAssociation
        }
    },
    computed: {
        modelHasEmptyAssociations(): boolean {
            let isInvalid = false
            this.dashboardAssociations?.some((association) => {
                if (association.fields.length == 0) isInvalid = true
            })
            return isInvalid
        }
    },
    setup() {
        const store = mainStore()
        const dashboardStore = dashStore()
        return { store, dashboardStore }
    },
    created() {
        console.log('STORE MODEL - datasetEditor.vue', this.dashboardStore.$state.dashboards[1])
        this.dashboardDatasets = deepcopy(this.dashboardStore.$state.dashboards[1].configuration.datasets)
        this.dashboardAssociations = deepcopy(this.dashboardStore.$state.dashboards[1].configuration.associations)
        this.getDatasets()
    },

    methods: {
        //#region ===================== Dataset Logic ====================================================
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
            console.log('DATASET ADDED - datasetEditor.vue -------', this.selectedDatasets)
        },
        //#endregion ===============================================================================================

        //#region ===================== Association Logic ====================================================
        createNewAssociation() {
            this.selectedAssociation = { fields: [], id: cryptoRandomString({ length: 16, type: 'base64' }) } as IAssociation
            this.dashboardAssociations.push(this.selectedAssociation)
        },
        deleteAssociation(associationId) {
            let index = this.dashboardAssociations.findIndex((association) => association.id === associationId)
            if (index !== -1) this.dashboardAssociations.splice(index, 1)
            this.selectedAssociation = null as any
        }
        //#endregion ===============================================================================================
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
.datasetEditor-tabs.p-tabview {
    overflow: auto;
    display: flex;
    flex-direction: column;
    flex: 1;
    .p-tabview-panels {
        overflow: auto;
        padding: 0;
        display: flex;
        flex-direction: column;
        flex: 1;
        .p-tabview-panel {
            overflow: auto;
            display: flex;
            flex: 1;
        }
    }
}
.details-warning-color {
    color: red;
}
</style>
