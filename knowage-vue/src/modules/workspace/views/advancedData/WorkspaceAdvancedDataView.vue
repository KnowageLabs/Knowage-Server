<template>
    <DataPreparationMonitoringDialog v-model:visibility="showMonitoring" @close="showMonitoring = false" @save="updateDatasetAndSave" :dataset="selectedDataset"></DataPreparationMonitoringDialog>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            {{ $t('workspace.advancedData.title') }}
        </template>
        <template #end>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <KnFabButton icon="fas fa-plus" data-test="new-folder-button" @click="showDataSetCatalog" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar p-ml-2" v-if="loading" data-test="progress-bar" />
    <KnDatasetList :visibility="showDatasetList" :items="availableDatasets" @selected="newDataPrep" @save="openDataPreparation(selectedDsForDataPrep)" @cancel="hideDataSetCatalog" />

    <div class="p-d-flex p-flex-row p-ai-center">
        <InputText class="kn-material-input p-m-2" :style="mainDescriptor.style.filterInput" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchItems" data-test="search-input" />
    </div>

    <div class="kn-overflow">
        <DataTable v-if="!toggleCardDisplay" class="p-datatable-sm kn-table p-mx-2" :value="preparedDatasets" :loading="loading" dataKey="objId" responsiveLayout="stack" breakpoint="600px" data-test="datasets-table">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <Column field="label" :header="$t('importExport.catalogFunction.column.label')" class="kn-truncated" :sortable="true" />
            <Column field="name" :header="$t('importExport.gallery.column.name')" class="kn-truncated" :sortable="true" />
            <Column field="tags" :header="$t('importExport.gallery.column.tags')" :sortable="true">
                <template #body="slotProps">
                    <span v-if="slotProps.data.tags.length > 0">
                        <Chip v-for="(tag, index) of slotProps.data.tags" :key="index"> {{ tag.name }} </Chip>
                    </span>
                </template>
            </Column>
            <Column :style="mainDescriptor.style.iconColumn">
                <template #header> &ensp; </template>
                <template #body="slotProps">
                    <Button icon="fas fa-ellipsis-v" class="p-button-link" @click.stop="showMenu($event, slotProps.data)" />
                    <Button icon="fas fa-info-circle" class="p-button-link" v-tooltip.left="$t('workspace.myModels.showInfo')" @click.stop="showSidebar(slotProps.data)" :data-test="'info-button-' + slotProps.data.name" />
                    <Button icon="fas fa-eye" class="p-button-link" @click.stop="previewDataset(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2" data-test="card-container">
            <Message v-if="preparedDatasets.length === 0" class="kn-flex p-m-2" severity="info" :closable="false" :style="mainDescriptor.style.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
            <template v-else>
                <WorkspaceCard
                    v-for="(dataset, index) of preparedDatasets"
                    :key="index"
                    :viewType="'dataset'"
                    :document="dataset"
                    @previewDataset="previewDataset"
                    @editFileDataset="editFileDataset"
                    @exportToXlsx="exportDataset($event, 'xls')"
                    @exportToCsv="exportDataset($event, 'csv')"
                    @shareDataset="shareDataset"
                    @cloneDataset="cloneDataset"
                    @deleteDataset="deleteDatasetConfirm"
                    @openDataPreparation="openDataPreparation"
                    @openSidebar="showSidebar"
                    @monitoring="showMonitoring = !showMonitoring"
                />
            </template>
        </div>
    </div>

    <DetailSidebar
        :visible="showDetailSidebar"
        :viewType="'dataset'"
        :document="selectedDataset"
        @previewDataset="previewDataset"
        @editFileDataset="editFileDataset"
        @exportToXlsx="exportDataset($event, 'xls')"
        @exportToCsv="exportDataset($event, 'csv')"
        @shareDataset="shareDataset"
        @cloneDataset="cloneDataset"
        @deleteDataset="deleteDatasetConfirm"
        @openDataPreparation="openDataPreparation"
        @close="showDetailSidebar = false"
        @monitoring="showMonitoring = !showMonitoring"
        data-test="detail-sidebar"
    />

    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
    <Menu id="creationMenu" ref="creationMenu" :model="creationMenuButtons" />

    <WorkspaceDataCloneDialog :visible="cloneDialogVisible" :propDataset="selectedDataset" @close="cloneDialogVisible = false" @clone="handleDatasetClone"></WorkspaceDataCloneDialog>
    <WorkspaceDataPreviewDialog :visible="previewDialogVisible" :propDataset="selectedDataset" @close="previewDialogVisible = false"></WorkspaceDataPreviewDialog>
    <WorkspaceWarningDialog :visible="warningDialogVisbile" :title="$t('workspace.advancedData.title')" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceWarningDialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import DataTable from 'primevue/datatable'
import KnDatasetList from '@/components/functionalities/KnDatasetList/KnDatasetList.vue'
import Column from 'primevue/column'
import Chip from 'primevue/chip'
import Menu from 'primevue/contextmenu'
import { IDataset } from '@/modules/workspace/Workspace'
import Message from 'primevue/message'
import WorkspaceDataCloneDialog from '@/modules/workspace/views/dataView/dialogs/WorkspaceDataCloneDialog.vue'
import WorkspaceDataPreviewDialog from '@/modules/workspace/views/dataView/dialogs/WorkspaceDataPreviewDialog.vue'
import WorkspaceWarningDialog from '@/modules/workspace/genericComponents/WorkspaceWarningDialog.vue'
import { AxiosResponse } from 'axios'
import DataPreparationMonitoringDialog from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoringDialog.vue'

export default defineComponent({
    components: { DataTable, KnDatasetList, Column, Chip, DataPreparationMonitoringDialog, DetailSidebar, WorkspaceCard, KnFabButton, WorkspaceDataCloneDialog, WorkspaceWarningDialog, WorkspaceDataPreviewDialog, Message, Menu },
    emits: ['toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    computed: {
        isDatasetOwner(): any {
            return (this.$store.state as any).user.userId === this.selectedDataset.owner
        },
        canLoadData(): any {
            if (this.selectedDataset.actions) {
                for (let i = 0; i < this.selectedDataset.actions.length; i++) {
                    const action = this.selectedDataset.actions[i]
                    if (action.name == 'loaddata') {
                        return true
                    }
                }
            }
            return false
        }
    },
    data() {
        return {
            mainDescriptor,
            loading: false,
            showDetailSidebar: false,
            showDatasetList: false as Boolean,
            showDatasetDialog: false,
            datasetList: [] as Array<IDataset>,
            preparedDatasets: [] as any,
            availableDatasets: [] as any,
            selectedDataset: {} as any,
            selectedDsForDataPrep: {} as any,
            menuButtons: [] as any,
            avroDatasets: [] as any,
            creationMenuButtons: [] as any,
            filters: {
                global: [filterDefault]
            } as Object,
            cloneDialogVisible: false,
            shareDialogVisible: false,
            previewDialogVisible: false,
            warningDialogVisbile: false,
            warningMessage: '',
            searchWord: '' as string,
            showMonitoring: false
        }
    },
    async created() {
        await this.getAllAvroDataSets()
        await this.getDatasets()
    },

    methods: {
        async loadDataset(datasetLabel: string) {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${datasetLabel}`)
                .then((response: AxiosResponse<any>) => {
                    this.selectedDataset = response.data[0]
                })
                .catch(() => {})
            this.loading = false
        },
        toggleDisplayView() {
            this.$emit('toggleDisplayView')
        },
        newDataPrep(dataset) {
            this.selectedDsForDataPrep = dataset
        },
        showSidebar(clickedDataset) {
            this.selectedDataset = clickedDataset
            this.showDetailSidebar = true
        },
        hideDataSetCatalog() {
            this.showDatasetList = false
            this.selectedDsForDataPrep = {}
        },
        showDataSetCatalog() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/for-dataprep`).then(
                (response: AxiosResponse<any>) => {
                    this.availableDatasets = [...response.data.root]
                    this.showDatasetList = true
                },
                () => {
                    this.$store.commit('setError', { title: 'Error', msg: 'Cannot load dataset list' })
                }
            )
        },
        showMenu(event, clickedDocument) {
            this.selectedDataset = clickedDocument
            this.createMenuItems(clickedDocument)
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        // prettier-ignore
        createMenuItems(clickedDocument: any) {
                let tmp = [] as any

                tmp.push(
                    { key: '0', label: this.$t('workspace.myData.xlsxExport'), icon: 'fas fa-file-excel', command: () => this.exportDataset(clickedDocument, 'xls'), visible: this.canLoadData && this.selectedDataset.dsTypeCd != 'File' },
                    { key: '1', label: this.$t('workspace.myData.csvExport'), icon: 'fas fa-file-csv', command: () => this.exportDataset(clickedDocument, 'csv'), visible: this.canLoadData && this.selectedDataset.dsTypeCd != 'File' },
                    { key: '4', label: this.$t('workspace.myData.deleteDataset'), icon: 'fas fa-trash', command: () => this.deleteDatasetConfirm(clickedDocument), visible: this.isDatasetOwner }
                )

                if ((this.$store.state as any).user?.functionalities.includes('DataPreparation')) {

                    tmp.push(
                        { key: '2', label: this.$t('workspace.myData.openDataPreparation'), icon: 'fas fa-cogs', command: () => this.openDataPreparation(clickedDocument), visible: true },
                        { key: '3', label: this.$t('workspace.myData.monitoring'), icon: 'pi pi-chart-line', command: () => this.handleMonitoring(clickedDocument), visible: true }
                    )
                }

                tmp = tmp.sort((a,b)=>a.key.localeCompare(b.key))
                this.menuButtons = tmp

        },
        createCreationMenuButtons() {
            this.creationMenuButtons = []
            this.creationMenuButtons.push({ key: '0', label: this.$t('workspace.myData.prepareData'), visible: true })
        },
        toggleDatasetDialog() {
            this.selectedDataset = {}
            this.showDatasetDialog = true
        },
        async previewDataset(dataset: any) {
            await this.loadDataset(dataset.label)
            this.previewDialogVisible = true
        },
        editFileDataset() {
            this.showDatasetDialog = true
        },
        handleMonitoring(dataset: any) {
            console.log(dataset)
            this.showMonitoring = !this.showMonitoring
        },
        openDataPreparation(dataset: any) {
            if (dataset.dsTypeCd == 'Prepared') {
                //edit existing data prep
                this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/advanced/${dataset.label}`).then(
                    (response: AxiosResponse<any>) => {
                        let instanceId = response.data.configuration.dataPrepInstanceId
                        this.$http.get(process.env.VUE_APP_DATA_PREPARATION_PATH + `1.0/process/by-instance-id/${instanceId}`).then(
                            (response: AxiosResponse<any>) => {
                                let transformations = response.data.definition
                                let processId = response.data.id
                                let datasetLabel = response.data.instance.dataSetLabel
                                if (this.isAvroReady(datasetLabel))
                                    // check if Avro file has been deleted or not
                                    this.$router.push({ name: 'data-preparation', params: { id: datasetLabel, transformations: JSON.stringify(transformations), processId: processId, instanceId: instanceId, dataset: JSON.stringify(dataset) } })
                                else {
                                    this.$store.commit('setInfo', {
                                        title: 'Avro file is missing',
                                        msg: 'Generate it again and then retry'
                                    })
                                }
                            },
                            () => {
                                this.$store.commit('setError', { title: 'Save error', msg: 'Cannot create process' })
                            }
                        )
                    },
                    () => {
                        this.$store.commit('setError', {
                            title: 'Cannot open data preparation'
                        })
                    }
                )
            } else if (this.isAvroReady(dataset.label)) {
                // original dataset already exported in Avro
                this.$router.push({ name: 'data-preparation', params: { id: dataset.label } })
            } else {
                this.$store.commit('setInfo', {
                    title: 'Avro file is missing',
                    msg: 'Generate it again and then retry'
                })
            }
        },
        isAvroReady(dsLabel: String) {
            if (this.avroDatasets.indexOf(dsLabel) >= 0) return true
            else return false
        },
        async getAllAvroDataSets() {
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/avro`)
                .then((response: AxiosResponse<any>) => {
                    this.avroDatasets = response.data
                })
                .catch(() => {})
        },
        async exportDataset(dataset: any, format: string) {
            this.loading = true
            //  { 'Content-Type': 'application/x-www-form-urlencoded' }
            await this.$http
                .post(
                    process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/export/dataset/${dataset.id}/${format}`,
                    {},
                    {
                        headers: {
                            Accept: 'application/json, text/plain, */*',
                            'Content-Type': 'application/json;charset=UTF-8'
                        }
                    }
                )
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('workspace.myData.exportSuccess')
                    })
                })
                .catch(() => {})
            this.loading = false
        },
        getFileType(type: string) {
            switch (type) {
                case 'csv':
                    return 'text/csv'
                case 'xls':
                    return 'application/vnd.ms-excel'
                case 'xlsx':
                    return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            }
        },
        shareDataset() {
            this.shareDialogVisible = true
        },
        async handleDatasetShare(dataset: any) {
            this.loading = true

            const url = dataset.catTypeId ? `selfservicedataset/share/?catTypeId=${dataset.catTypeId}&id=${dataset.id}` : `selfservicedataset/share/?id=${dataset.id}`

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    this.shareDialogVisible = false
                    this.getDatasets()
                })
                .catch(() => {})
            this.loading = false
        },
        async cloneDataset(dataset: any) {
            await this.loadDataset(dataset.label)
            this.cloneDialogVisible = true
        },
        async handleDatasetClone(dataset: any) {
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets`, dataset, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    this.cloneDialogVisible = false
                    this.getDatasets()
                })
                .catch((response: any) => {
                    this.warningDialogVisbile = true
                    this.warningMessage = response
                })
        },
        datasetPreparation(dataset: any) {
            this.$router.push({ name: 'data-preparation', params: { id: dataset.label } })
        },

        deleteDatasetConfirm(dataset: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => await this.deleteDataset(dataset)
            })
        },
        async deleteDataset(dataset: any) {
            this.loading = true
            await this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${dataset.label}`)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    this.getDatasets()
                })
                .catch(() => {})
            this.loading = false
        },
        closeWarningDialog() {
            this.warningMessage = ''
            this.warningDialogVisbile = false
        },
        async getDatasets() {
            this.loading = true
            this.searchWord = ''
            this.preparedDatasets = this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/advanced`)
                .then((response: AxiosResponse<any>) => {
                    this.datasetList = [...response.data.root]
                    this.preparedDatasets = [...this.datasetList]
                })
                .finally(() => (this.loading = false))
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.preparedDatasets = [...this.datasetList] as any[]
                } else {
                    this.preparedDatasets = this.datasetList.filter((el: any) => {
                        return el.label?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.dsTypeCd?.toLowerCase().includes(this.searchWord.toLowerCase())
                    })
                }
            }, 250)
        },
        async updateDatasetAndSave(newConfig) {
            this.showMonitoring = false

            await this.$http.patch(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/instance/' + newConfig.instanceId, { config: newConfig.config }).then(
                () => {
                    this.loadDataset(this.selectedDataset.label)
                },
                () => {
                    this.$store.commit('setError', { title: this.$t('common.error.saving'), msg: this.$t('managers.workspaceManagement.dataPreparation.errors.updatingSchedulation') })
                }
            )
        }
    }
})
</script>
