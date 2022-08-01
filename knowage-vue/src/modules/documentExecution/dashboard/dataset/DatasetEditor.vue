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
            <!-- <div id="logsnstuff" style="height: 300px; overflow: auto">
                {{ dashboardAssociations }}
                <br />
                {{ dashboardDatasets }}
                <br />
                selected -------------
                <br />
                {{ selectedDatasets.length }}
            </div>
            <br /> -->

            <TabView v-if="!loading" class="datasetEditor-tabs">
                <TabPanel :header="$t('dashboard.datasetEditor.dataTabTitle')">
                    <DataTab :availableDatasetsProp="availableDatasets" :dashboardDatasetsProp="dashboardDatasets" :selectedDatasetsProp="selectedDatasets" :documentDriversProp="filtersDataPropMock" @addSelectedDatasets="addSelectedDatasets" @deleteDataset="confirmDeleteDataset" />
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span v-bind:class="{ 'details-warning-color': modelHasEmptyAssociations }">{{ $t('dashboard.datasetEditor.associationsTabTitle') }}</span>
                        <i v-if="modelHasEmptyAssociations" class="fa-solid fa-circle-exclamation p-ml-1" v-bind:class="{ 'details-warning-color': modelHasEmptyAssociations }" />
                    </template>
                    <AssociationsTab
                        :dashboardAssociationsProp="dashboardAssociations"
                        :selectedDatasetsProp="selectedDatasets"
                        :selectedAssociationProp="selectedAssociation"
                        @createNewAssociation="createNewAssociation"
                        @associationDeleted="deleteAssociation"
                        @addIndexesOnAssociations="addIndexesOnAssociations"
                    />
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
import { IAssociation, IModelDataset, IModelDatasetParameter, IAssociationField } from '../Dashboard'
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
    props: { availableDatasetsProp: { required: true, type: Array }, filtersDataProp: { type: Object } },
    emits: ['closeDatasetEditor', 'datasetEditorSaved'],
    data() {
        return {
            loading: false,
            availableDatasets: {} as any,
            dashboardDatasets: [] as IModelDataset[],
            selectedDatasets: [] as any,
            dashboardAssociations: [] as IAssociation[],
            selectedAssociation: {} as IAssociation,
            filtersDataPropMock: [
                {
                    urlName: '123',
                    metadata: {},
                    visible: true,
                    valueSelection: 'man_in',
                    showOnPanel: 'true',
                    driverUseLabel: 'Manual Input',
                    label: 'test driver',
                    driverDefaultValue: null,
                    type: 'STRING',
                    driverLabel: 'Manual Input String',
                    mandatory: false,
                    allowInternalNodeSelection: false,
                    multivalue: false,
                    dependencies: {
                        data: [],
                        visual: [],
                        lov: []
                    },
                    selectionType: '',
                    id: 7700,
                    parameterValue: [
                        {
                            value: '',
                            description: ''
                        }
                    ]
                },
                {
                    urlName: 'testdriver2',
                    metadata: {
                        colsMap: {
                            _col0: 'product_family'
                        },
                        descriptionColumn: 'product_family',
                        invisibleColumns: [],
                        valueColumn: 'product_family',
                        visibleColumns: ['product_family']
                    },
                    visible: true,
                    data: [
                        {
                            value: 'Car',
                            description: 'Car'
                        },
                        {
                            value: 'Drink',
                            description: 'Drink'
                        },
                        {
                            value: 'Food',
                            description: 'Food'
                        },
                        {
                            value: 'Non-Consumable',
                            description: 'Non-Consumable'
                        }
                    ],
                    valueSelection: 'lov',
                    showOnPanel: 'true',
                    driverUseLabel: 'ALL',
                    label: 'driver2',
                    driverDefaultValue: [
                        {
                            _col0: 'Car'
                        }
                    ],
                    type: 'STRING',
                    driverLabel: 'DEMO_ProductFamily',
                    mandatory: false,
                    allowInternalNodeSelection: false,
                    multivalue: false,
                    dependencies: {
                        data: [],
                        visual: [],
                        lov: []
                    },
                    selectionType: 'COMBOBOX',
                    id: 7701,
                    parameterDescription: ['Car'],
                    parameterValue: [
                        {
                            value: 'Car',
                            description: 'Car'
                        }
                    ]
                }
            ] as any
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
        //#region ===================== Dataset Logic ====================================================
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
        },

        //#region ===================== DELETE DATASET ====================================================
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
                        this.unselectAssociation()
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
        //#endregion ===============================================================================================
        //#endregion ===============================================================================================

        //#region ===================== Association Logic ====================================================
        createNewAssociation() {
            this.selectedAssociation = { fields: [], id: cryptoRandomString({ length: 16, type: 'base64' }) } as IAssociation
            this.dashboardAssociations.push(this.selectedAssociation)
        },
        deleteAssociation(associationId) {
            let index = this.dashboardAssociations.findIndex((association) => association.id === associationId)
            if (index !== -1) this.dashboardAssociations.splice(index, 1)
            this.unselectAssociation()
        },
        addIndexesOnAssociations() {
            console.log('ALL ASSOCIATION -----', this.dashboardAssociations)

            let selectedFields = {}
            this.dashboardAssociations.forEach((association) => {
                association.fields.reduce((obj, item) => {
                    obj[item.dataset] = obj[item.dataset] || []
                    obj[item.dataset].push(item.column)
                    return obj
                }, selectedFields)
            })
            console.log('MAPPED/REDUCED -----', selectedFields)

            this.selectedDatasets.forEach((dataset) => {
                dataset.modelIndexes ? '' : (dataset.modelIndexes = [])
                selectedFields[dataset.id.dsId]
                    .filter((item) => dataset.modelIndexes.indexOf(item) == -1)
                    .forEach((index) => {
                        dataset.modelIndexes.push(index)
                    })
            })
        },
        unselectAssociation() {
            this.selectedAssociation = null as any
        },
        //#endregion ===============================================================================================
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
