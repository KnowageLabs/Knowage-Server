<template>
    <Teleport to=".dashboard-container">
        <div class="datasetEditor">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ $t('dashboard.datasetEditor.title') }} </template>
                <template #end>
                    <Button :disabled="modelHasEmptyAssociations" icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="saveDatasetsToModel" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('closeDatasetEditor')" />
                </template>
            </Toolbar>

            <TabView v-if="!loading" class="datasetEditor-tabs">
                <TabPanel :header="$t('dashboard.datasetEditor.dataTabTitle')">
                    <DataTab :availableDatasetsProp="availableDatasets" :dashboardDatasetsProp="dashboardDatasets" :selectedDatasetsProp="selectedDatasets" :documentDriversProp="filtersDataProp" @addSelectedDatasets="addSelectedDatasets" @deleteDataset="confirmDeleteDataset" />
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span v-bind:class="{ 'details-warning-color': modelHasEmptyAssociations }">{{ $t('dashboard.datasetEditor.associationsTabTitle') }}</span>
                        <i v-if="modelHasEmptyAssociations" class="fa-solid fa-circle-exclamation p-ml-1 details-warning-color" />
                    </template>
                    <AssociationsTab
                        :dashboardAssociationsProp="dashboardAssociations"
                        :selectedDatasetsProp="selectedDatasets"
                        :selectedAssociationProp="selectedAssociation"
                        @createNewAssociation="createNewAssociation"
                        @associationDeleted="deleteAssociation"
                        @associationSelected="selectAssociation"
                        @addIndexesOnAssociations="addIndexesOnAssociations"
                    />
                </TabPanel>
            </TabView>
        </div>
    </Teleport>
    <DriverWarningDialog :visible="warningDialogVisible" :ignoredDatasets="ignoredDatasets" @close="warningDialogVisible = false" />
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the dataset.
 */
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { IAssociation, IModelDataset, IModelDatasetParameter, IAssociationField } from '../Dashboard'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DataTab from './DatasetEditorDataTab/DatasetEditorDataTab.vue'
import AssociationsTab from './DatasetEditorAssociations/DatasetEditorAssociations.vue'
import mainStore from '../../../../App.store'
import dashStore from '../Dashboard.store'
import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'
import DriverWarningDialog from './DatasetEditorDataTab/DatasetEditorDataDialog/DatasetEditorDataWarningDialog.vue'

export default defineComponent({
    name: 'dataset-editor',
    components: { TabView, TabPanel, DataTab, AssociationsTab, DriverWarningDialog },
    props: { availableDatasetsProp: { required: true, type: Array }, filtersDataProp: { type: Object } },
    emits: ['closeDatasetEditor', 'datasetEditorSaved'],
    data() {
        return {
            loading: false,
            warningDialogVisible: false,
            availableDatasets: {} as any,
            dashboardDatasets: [] as IModelDataset[],
            selectedDatasets: [] as any,
            dashboardAssociations: [] as IAssociation[],
            selectedAssociation: {} as IAssociation,
            ignoredDatasets: [] as string[]
        }
    },
    watch: {
        selectedDatasets: {
            handler() {
                console.log('SELECTED DATASETS CHANGED', this.selectedDatasets)
            },
            deep: true
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
    async created() {
        await this.setDatasetsData()
    },

    methods: {
        async setDatasetsData() {
            this.availableDatasets = deepcopy(this.availableDatasetsProp)
            this.dashboardDatasets = deepcopy(this.dashboardStore.$state.dashboards[1].configuration.datasets)
            this.dashboardAssociations = deepcopy(this.dashboardStore.$state.dashboards[1].configuration.associations)
            this.selectedDatasets = this.selectModelDatasetsFromAvailable()
            this.setDatasetParametersFromModel()
        },
        selectModelDatasetsFromAvailable() {
            return this.availableDatasets?.filter((responseDataset) => {
                return this.dashboardDatasets?.find((dashboardDataset) => {
                    if (responseDataset.id.dsId === dashboardDataset.id) {
                        responseDataset.modelParams = dashboardDataset.parameters
                        responseDataset.modelCache = dashboardDataset.cache
                        responseDataset.modelIndexes = dashboardDataset.indexes

                        return responseDataset
                    }
                })
            })
        },
        //TODO: Improve this method
        setDatasetParametersFromModel() {
            this.selectedDatasets.forEach((dataset) => {
                if (dataset.parameters.length > 0 && dataset.modelParams.length > 0) {
                    dataset.parameters.forEach((parameter) => {
                        dataset.modelParams.forEach((modelParam) => {
                            if (parameter.name === modelParam.name) {
                                parameter.value = modelParam.value
                                parameter.modelType = modelParam.type
                            }
                        })
                    })
                }
            })
        },
        addSelectedDatasets(datasetsToAdd) {
            if ((this.selectedDatasets.some((dataset) => dataset.drivers?.length > 0) && datasetsToAdd.some((dataset) => dataset.drivers?.length > 0)) || datasetsToAdd.filter((dataset) => dataset.drivers?.length > 0).length > 1) {
                this.selectedDatasets.push(...datasetsToAdd.filter((dataset) => !(dataset.drivers?.length > 0)))
                this.ignoredDatasets = datasetsToAdd.filter((dataset) => dataset.drivers?.length > 0).map((dataset) => dataset.name)
                this.warningDialogVisible = true
            } else {
                datasetsToAdd.forEach((dataset) => {
                    this.selectedDatasets.push(dataset)
                    const formattedDatasetForDashboard = {
                        id: dataset.id.dsId,
                        indexes: [],
                        cache: false,
                        parameters: []
                    } as IModelDataset
                    this.dashboardDatasets.push(formattedDatasetForDashboard)
                })
            }
        },
        selectAssociation(association) {
            this.selectedAssociation = association
        },

        confirmDeleteDataset(datasetToDelete) {
            //TODO: Check if widget is using a dataset
            let datasetUsedByWidgetCheck = false
            if (datasetUsedByWidgetCheck) {
                this.store.setInfo({ title: this.$t('common.toast.error'), msg: 'Dataset is being used by some widget.' })
            } else {
                this.$confirm.require({
                    message: this.$t('documentExecution.dossier.deleteConfirm'),
                    header: this.$t('documentExecution.dossier.deleteTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.selectedAssociation = null as any
                        this.checkForDatasetAssociations(datasetToDelete)
                    }
                })
            }
        },
        async checkForDatasetAssociations(datasetToDelete) {
            let datasetAssociations = (await this.getDatasetAssociations(datasetToDelete.id.dsId)) as unknown as IAssociation[]
            if (datasetAssociations && datasetAssociations.length > 0) this.deleteDatasetAssociations(datasetAssociations)
            this.deleteDataset(datasetToDelete.id.dsId)
        },
        async getDatasetAssociations(datasetId) {
            return this.dashboardAssociations?.filter((association) => {
                return association.fields.find((field) => field.dataset === datasetId)
            })
        },
        deleteDatasetAssociations(associationsToDelete) {
            this.dashboardAssociations = this.dashboardAssociations.filter((association) => !associationsToDelete.find((assToDelete) => assToDelete.id === association.id))
        },
        deleteDataset(datasetToDeleteId) {
            let toDeleteIndex = this.selectedDatasets.findIndex((dataset) => datasetToDeleteId === dataset.id.dsId)
            this.selectedDatasets.splice(toDeleteIndex, 1)
        },

        saveDatasetsToModel() {
            let formattedDatasets = [] as IModelDataset[]

            this.selectedDatasets.forEach((dataset) => {
                formattedDatasets.push(this.formatDatasetForModel(dataset))
            })

            this.dashboardStore.$state.dashboards[1].configuration.datasets = formattedDatasets
            this.dashboardStore.$state.dashboards[1].configuration.associations = this.dashboardAssociations

            this.$emit('datasetEditorSaved')
        },
        formatDatasetForModel(datasetToFormat) {
            let formattedDataset = {
                id: datasetToFormat.id.dsId,
                cache: datasetToFormat.modelCache ?? false,
                indexes: datasetToFormat.modelCache ? datasetToFormat.modelIndexes : [],
                parameters: datasetToFormat.parameters.map((parameter) => {
                    return { name: parameter.name, type: parameter.modelType, value: parameter.value, multivalue: parameter.multivalue ?? false } as IModelDatasetParameter
                })
            } as IModelDataset

            return formattedDataset
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
