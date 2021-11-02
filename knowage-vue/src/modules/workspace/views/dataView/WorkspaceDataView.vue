<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary" style="width:100%">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.myData.title') }}
        </template>
        <template #right>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <KnFabButton icon="fas fa-plus" data-test="new-folder-button" @click="showCreationMenu" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar p-ml-2" v-if="loading" data-test="progress-bar" />

    <InputText class="kn-material-input p-m-2" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />

    <div class="overflow">
        <DataTable v-if="!toggleCardDisplay" style="width:100%" class="p-datatable-sm kn-table" :value="allDataset" :loading="loading" dataKey="objId" responsiveLayout="stack" breakpoint="600px" v-model:filters="filters">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <Column field="label" :header="$t('importExport.catalogFunction.column.label')" class="kn-truncated" :sortable="true" />
            <Column field="name" :header="$t('importExport.gallery.column.name')" class="kn-truncated" :sortable="true" />
            <Column field="dsTypeCd" :header="$t('importExport.gallery.column.type')" :sortable="true" />
            <Column field="tags" :header="$t('importExport.gallery.column.tags')" :sortable="true">
                <template #body="slotProps">
                    <span v-if="slotProps.data.tags.length > 0">
                        <Chip v-for="(tag, index) of slotProps.data.tags" :key="index"> {{ tag.name }} </Chip>
                    </span>
                </template>
            </Column>
            <Column :header="$t('workspace.myData.parametrical')">
                <template #body="slotProps">
                    <i v-if="slotProps.data.pars.length > 0" class="fas fa-check p-button-link" />
                </template>
            </Column>
            <Column field="tags" :header="$t('workspace.myData.driverable')">
                <template #body="slotProps">
                    <i v-if="slotProps.data.drivers.length > 0" class="fas fa-check p-button-link" />
                </template>
            </Column>
            <Column :style="mainDescriptor.style.iconColumn">
                <template #header> &ensp; </template>
                <template #body="slotProps">
                    <Button icon="fas fa-ellipsis-v" class="p-button-link" @click="showMenu($event, slotProps.data)" />
                    <Button icon="fas fa-info-circle" class="p-button-link" v-tooltip.left="$t('workspace.myModels.showInfo')" @click="showSidebar(slotProps.data)" />
                    <Button icon="fas fa-eye" class="p-button-link" @click="previewDataset(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2">
            <WorkspaceCard
                v-for="(dataset, index) of allDataset"
                :key="index"
                :viewType="'dataset'"
                :document="dataset"
                @previewDataset="previewDataset"
                @editFileDataset="editFileDataset"
                @openDatasetInQBE="openDatasetInQBE"
                @exportToXlsx="exportToXlsx"
                @exportToCsv="exportToCsv"
                @downloadDatasetFile="downloadDatasetFile"
                @shareDataset="shareDataset"
                @cloneDataset="cloneDataset"
                @deleteDataset="deleteDatasetConfirm"
                @openSidebar="showSidebar"
            />
        </div>
    </div>

    <DetailSidebar
        :visible="showDetailSidebar"
        :viewType="'dataset'"
        :document="selectedDataset"
        :datasetCategories="datasetCategories"
        @previewDataset="previewDataset"
        @editFileDataset="editFileDataset"
        @openDatasetInQBE="openDatasetInQBE"
        @exportToXlsx="exportToXlsx"
        @exportToCsv="exportToCsv"
        @downloadDatasetFile="downloadDatasetFile"
        @shareDataset="shareDataset"
        @cloneDataset="cloneDataset"
        @deleteDataset="deleteDatasetConfirm"
        @close="showDetailSidebar = false"
    />

    <DatasetWizard v-if="showDatasetDialog" :selectedDataset="selectedDataset" :visible="showDatasetDialog" @closeDialog="showDatasetDialog = false" />
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
    <Menu id="creationMenu" ref="creationMenu" :model="creationMenuButtons" />

    <WorkspaceDataCloneDialog :visible="cloneDialogVisible" :propDataset="selectedDataset" @close="cloneDialogVisible = false" @clone="handleDatasetClone"></WorkspaceDataCloneDialog>
    <WorkspaceDataShareDialog :visible="shareDialogVisible" :propDataset="selectedDataset" :datasetCategories="datasetCategories" @close="shareDialogVisible = false" @share="handleDatasetShare"></WorkspaceDataShareDialog>
    <WorkspaceWarningDialog :visible="warningDialogVisbile" :title="$t('workspace.myData.title')" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceWarningDialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import DatasetWizard from './datasetWizard/WorkspaceDatasetWizardContainer.vue'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Chip from 'primevue/chip'
import Menu from 'primevue/contextmenu'
import WorkspaceDataCloneDialog from './dialogs/WorkspaceDataCloneDialog.vue'
import WorkspaceDataShareDialog from './dialogs/WorkspaceDataShareDialog.vue'
import WorkspaceWarningDialog from '../../genericComponents/WorkspaceWarningDialog.vue'
import { AxiosResponse } from 'axios'
import { downloadDirect } from '@/helpers/commons/fileHelper'

export default defineComponent({
    components: { DataTable, Column, Chip, DetailSidebar, WorkspaceCard, Menu, KnFabButton, DatasetWizard, WorkspaceDataCloneDialog, WorkspaceWarningDialog, WorkspaceDataShareDialog },
    emits: ['toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    computed: {
        isDatasetOwner(): any {
            return (this.$store.state as any).user.fullName === this.selectedDataset.owner
        },
        showQbeEditButton(): any {
            return (this.$store.state as any).user.fullName === this.selectedDataset.owner && (this.selectedDataset.dsTypeCd == 'Federated' || this.selectedDataset.dsTypeCd == 'Qbe')
        },
        datasetHasDrivers(): any {
            return this.selectedDataset.drivers && this.selectedDataset.length > 0
        },
        datasetHasParams(): any {
            return this.selectedDataset.pars && this.selectedDataset.pars > 0
        },
        datasetIsIterable(): any {
            // in order to export to XLSX, dataset must implement an iterator (BE side)
            let notIterableDataSets = ['Federated']
            if (notIterableDataSets.includes(this.selectedDataset.dsTypeCd)) return false
            else return true
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
            showDatasetDialog: false,
            allDataset: [] as any,
            datasetCategories: [] as any,
            selectedDataset: {} as any,
            menuButtons: [] as any,
            creationMenuButtons: [] as any,
            filters: {
                global: [filterDefault]
            } as Object,
            cloneDialogVisible: false,
            shareDialogVisible: false,
            warningDialogVisbile: false,
            warningMessage: ''
        }
    },
    created() {
        this.getAllData()
    },
    methods: {
        async getAllData() {
            await this.getAllDatasets()
            await this.getDatasetCategories()
            this.loading = false
        },
        async getAllDatasets() {
            this.loading = true
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/mydata/`).then((response: AxiosResponse<any>) => {
                this.allDataset = [...response.data.root]
            })
        },
        async getDatasetCategories() {
            this.loading = true
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domainsforfinaluser/ds-categories`).then((response: AxiosResponse<any>) => {
                this.datasetCategories = [...response.data]
            })
        },
        async loadDataset(datasetLabel: string) {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${datasetLabel}`)
                .then((response: AxiosResponse<any>) => {
                    this.selectedDataset = response.data[0]
                })
                .catch(() => {})
            this.loading = false
            console.log('LOADED GET ONE DATASET: ', this.selectedDataset)
        },
        toggleDisplayView() {
            this.$emit('toggleDisplayView')
        },
        showSidebar(clickedDataset) {
            this.selectedDataset = clickedDataset
            this.showDetailSidebar = true
        },
        showCreationMenu(event) {
            this.createCreationMenuButtons()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.creationMenu.toggle(event)
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
            this.menuButtons = []
            this.menuButtons.push(
                { key: '0', label: this.$t('workspace.myAnalysis.menuItems.showDsDetails'), icon: 'fas fa-pen', command: this.editFileDataset, visible: this.isDatasetOwner && this.selectedDataset.dsTypeCd == 'File' },
                { key: '1', label: this.$t('workspace.myModels.openInQBE'), icon: 'fas fa-pen', command: this.openDatasetInQBE, visible: this.showQbeEditButton },
                { key: '2', label: this.$t('workspace.myData.xlsxExport'), icon: 'fas fa-file-excel', command: () => this.exportDataset(clickedDocument, 'xls'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.selectedDataset.dsTypeCd != 'File' && this.datasetIsIterable },
                { key: '3', label: this.$t('workspace.myData.csvExport'), icon: 'fas fa-file-csv', command: () => this.exportDataset(clickedDocument, 'csv'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.selectedDataset.dsTypeCd != 'File' },
                { key: '4', label: this.$t('workspace.myData.fileDownload'), icon: 'fas fa-download', command: () => this.downloadDatasetFile(clickedDocument), visible: this.selectedDataset.dsTypeCd == 'File' },
                { key: '5', label: this.$t('workspace.myData.shareDataset'), icon: 'fas fa-share-alt', command: () => this.shareDataset(clickedDocument), visible: this.canLoadData && this.isDatasetOwner },
                { key: '6', label: this.$t('workspace.myData.cloneDataset'), icon: 'fas fa-clone', command: () => this.cloneDataset(clickedDocument), visible: this.canLoadData && this.selectedDataset.dsTypeCd == 'Qbe' },
                { key: '7', label: this.$t('workspace.myData.deleteDataset'), icon: 'fas fa-trash', command: () => this.deleteDatasetConfirm(clickedDocument), visible: this.isDatasetOwner }
            )

        },
        createCreationMenuButtons() {
            this.creationMenuButtons = []
            this.creationMenuButtons.push(
                { key: '0', label: this.$t('managers.businessModelManager.uploadFile'), command: this.toggleDatasetDialog, visible: true },
                { key: '1', label: this.$t('workspace.myData.prepareData'), command: this.openDatasetInQBE, visible: true },
                { key: '2', label: this.$t('workspace.myData.openData'), command: this.exportDataset, visible: true }
            )
        },
        toggleDatasetDialog() {
            this.selectedDataset = {}
            this.showDatasetDialog = true
        },
        previewDataset(event) {
            console.log('previewDataset(event) {', event)
        },
        editFileDataset() {
            this.showDatasetDialog = true
        },
        openDatasetInQBE(event) {
            console.log('openDatasetInQBE(event)', event)
            this.$store.commit('setInfo', {
                title: 'Todo',
                msg: 'Functionality not in this sprint'
            })
        },
        async exportDataset(dataset: any, format: string) {
            console.log('export dataset ', dataset, ', format: ', format)
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
        async downloadDatasetFile(dataset: any) {
            //  console.log('Download Dataset File', dataset)
            await this.loadDataset(dataset.label)
            // console.log('SELECTED DS', this.selectedDataset)
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/download/file?dsLabel=${this.selectedDataset.label}&type=${this.selectedDataset.fileType.toLowerCase()}`, {
                    headers: {
                        Accept: 'application/json, text/plain, */*'
                    },
                    responseType: 'blob'
                })
                .then((response: AxiosResponse<any>) => {
                    if (response.data.errors) {
                        this.$store.commit('setError', {
                            title: this.$t('common.error.downloading'),
                            msg: this.$t('common.error.downloading')
                        })
                    } else {
                        downloadDirect(response.data, this.selectedDataset.label, this.getFileType(this.selectedDataset.fileType.toLowerCase()))
                        this.$store.commit('setInfo', { title: this.$t('common.toast.success') })
                    }
                })
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
        shareDataset(dataset: any) {
            console.log('SHARE DATASET BEGIN: ', dataset)
            this.shareDialogVisible = true
        },
        async handleDatasetShare(dataset: any) {
            console.log('SHARE DATASET END: ', dataset)
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
                    this.getAllData()
                })
                .catch(() => {})
            this.loading = false
        },
        async cloneDataset(dataset: any) {
            console.log('CLONE DATASET BEGIN', dataset)
            await this.loadDataset(dataset.label)
            this.cloneDialogVisible = true
        },
        async handleDatasetClone(dataset: any) {
            console.log('DATSET FOR CLONE END: ', dataset)
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets`, dataset, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    this.cloneDialogVisible = false
                    this.getAllData()
                })
                .catch((response: any) => {
                    this.warningDialogVisbile = true
                    this.warningMessage = response
                })
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
            // console.log('deleteDataset ', dataset)
            this.loading = true
            await this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${dataset.label}`)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    this.getAllData()
                })
                .catch(() => {})
            this.loading = false
        },
        closeWarningDialog() {
            this.warningMessage = ''
            this.warningDialogVisbile = false
        }
    }
})
</script>
