<template>
    <Teleport to=".dashboard-container">
        <div class="dashboardEditor">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ $t('dashboard.datasetEditor.title') }} </template>
                <template #end>
                    <Button :disabled="modelHasEmptyAssociations" icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="saveDatasetsToModel" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('closeDatasetEditor')" />
                </template>
            </Toolbar>

            <TabView v-if="!loading" class="dashboardEditor-tabs">
                <TabPanel :header="$t('dashboard.datasetEditor.dataTabTitle')">
                    <DataTab
                        :availableDatasetsProp="availableDatasets"
                        :dashboardDatasetsProp="dashboardDatasets"
                        :selectedDatasetsProp="selectedDatasets"
                        :documentDriversProp="filtersDataProp"
                        :dashboardId="dashboardIdProp"
                        @addSelectedDatasets="addSelectedDatasets"
                        @deleteDataset="confirmDeleteDataset"
                    />
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
import { IAssociation, IDashboardDataset, IDashboardDatasetParameter } from '../Dashboard'
import { loadDatasets } from '../DashboardHelpers'
import { mapActions } from 'pinia'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DataTab from './DatasetEditorDataTab/DatasetEditorDataTab.vue'
import AssociationsTab from './DatasetEditorAssociations/DatasetEditorAssociations.vue'
import DriverWarningDialog from './DatasetEditorDataTab/DatasetEditorDataDialog/DatasetEditorDataWarningDialog.vue'
import mainStore from '../../../../App.store'
import dashStore from '../Dashboard.store'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'dataset-editor',
    components: { TabView, TabPanel, DataTab, AssociationsTab, DriverWarningDialog },
    props: { availableDatasetsProp: { required: true, type: Array }, filtersDataProp: { type: Object }, dashboardIdProp: { type: String, required: true } },
    emits: ['closeDatasetEditor', 'datasetEditorSaved', 'allDatasetsLoaded'],
    data() {
        return {
            activeIndex: 0,
            loading: false,
            warningDialogVisible: false,
            availableDatasets: {} as any,
            dashboardDatasets: [] as IDashboardDataset[],
            selectedDatasets: [] as any,
            dashboardAssociations: [] as IAssociation[],
            selectedAssociation: {} as any,
            ignoredDatasets: [] as string[],
            uncachedDatasets: ['SbiQueryDataSet', 'SbiQbeDataSet', 'SbiSolrDataSet', 'SbiPreparedDataSet']
        }
    },
    watch: {
        async availableDatasetsProp() {
            await this.setDatasetsData()
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
        ...mapActions(dashStore, ['setAllDatasets', 'getAllDatasetLoadedFlag', 'setAllDatasetLoadedFlag']),
        async setDatasetsData() {
            await this.loadAvailableDatasets()
            this.dashboardDatasets = deepcopy(this.dashboardStore.$state.dashboards[this.dashboardIdProp].configuration.datasets)
            this.dashboardAssociations = deepcopy(this.dashboardStore.$state.dashboards[this.dashboardIdProp].configuration.associations)
            this.selectedDatasets = this.selectModelDatasetsFromAvailable()
            this.setDatasetParametersFromModel()
            this.setDatasetDriversFromModel()
        },
        async loadAvailableDatasets() {
            this.availableDatasets = deepcopy(this.availableDatasetsProp)
            if (this.getAllDatasetLoadedFlag(this.dashboardIdProp)) return
            this.availableDatasets = await loadDatasets(null, this.store, this.setAllDatasets, this.$http)
            this.$emit('allDatasetsLoaded', this.availableDatasets)
            this.setAllDatasetLoadedFlag(this.dashboardIdProp, true)
        },
        selectModelDatasetsFromAvailable() {
            return this.availableDatasets?.filter((responseDataset) => {
                return this.dashboardDatasets?.find((dashboardDataset) => {
                    if (responseDataset.id.dsId === dashboardDataset.id) {
                        responseDataset.modelParams = dashboardDataset.parameters
                        responseDataset.modelDrivers = dashboardDataset.drivers ? dashboardDataset.drivers : []
                        responseDataset.modelCache = dashboardDataset.cache
                        responseDataset.modelIndexes = dashboardDataset.indexes

                        return responseDataset
                    }
                })
            })
        },
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
        setDatasetDriversFromModel() {
            this.selectedDatasets.forEach((dataset) => {
                if (dataset.drivers && dataset.modelDrivers) {
                    dataset.formattedDrivers = dataset.modelDrivers
                }
            })
        },
        addSelectedDatasets(datasetsToAdd) {
            this.setDatasetCache(datasetsToAdd)
            if ((this.selectedDatasets.some((dataset) => dataset.drivers?.length > 0) && datasetsToAdd.some((dataset) => dataset.drivers?.length > 0)) || datasetsToAdd.filter((dataset) => dataset.drivers?.length > 0).length > 1) {
                this.selectedDatasets.push(...datasetsToAdd.filter((dataset) => !(dataset.drivers?.length > 0)))
                this.ignoredDatasets = datasetsToAdd.filter((dataset) => dataset.drivers?.length > 0).map((dataset) => dataset.name)
                this.warningDialogVisible = true
            } else {
                datasetsToAdd.forEach((dataset) => {
                    this.selectedDatasets.push(dataset)
                    const formattedDatasetForDashboard = {
                        id: dataset.id.dsId,
                        label: dataset.label,
                        dsLabel: dataset.label,
                        indexes: [],
                        drivers: [],
                        cache: true,
                        parameters: []
                    } as IDashboardDataset
                    this.dashboardDatasets.push(formattedDatasetForDashboard)
                })
            }
        },
        setDatasetCache(datasets) {
            datasets.forEach((dataset) => {
                this.uncachedDatasets.includes(dataset.type) ? (dataset.modelCache = false) : (dataset.modelCache = true)
            })
        },
        selectAssociation(association) {
            this.selectedAssociation = association
        },

        confirmDeleteDataset(datasetToDelete) {
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
            let formattedDatasets = [] as IDashboardDataset[]

            this.selectedDatasets.forEach((dataset) => {
                formattedDatasets.push(this.formatDatasetForModel(dataset))
            })

            this.dashboardStore.$state.dashboards[this.dashboardIdProp].configuration.datasets = formattedDatasets
            this.dashboardStore.$state.dashboards[this.dashboardIdProp].configuration.associations = this.dashboardAssociations

            this.$emit('datasetEditorSaved')
        },
        formatDatasetForModel(datasetToFormat) {
            let formattedDataset = {
                id: datasetToFormat.id.dsId,
                dsLabel: datasetToFormat.label,
                cache: datasetToFormat.modelCache ?? false,
                indexes: datasetToFormat.modelCache ? datasetToFormat.modelIndexes : [],
                parameters: datasetToFormat.parameters.map((parameter) => {
                    return { name: parameter.name, type: parameter.modelType, value: parameter.value, multivalue: parameter.multivalue ?? false } as IDashboardDatasetParameter
                })
            } as IDashboardDataset

            if (datasetToFormat.formattedDrivers && datasetToFormat.formattedDrivers.length > 0) {
                formattedDataset.drivers = datasetToFormat.formattedDrivers
            }

            return formattedDataset
        }
    }
})
</script>
