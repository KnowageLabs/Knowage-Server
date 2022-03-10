<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary" :style="mainDescriptor.style.maxWidth">
        <template #start>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.myData.title') }}
        </template>
        <template #end>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <KnFabButton v-if="tableMode === 'My Datasets'" icon="fas fa-plus" data-test="new-folder-button" @click="showCreationMenu" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar p-ml-2" v-if="loading" data-test="progress-bar" />

    <div class="p-d-flex p-flex-row p-ai-center">
        <InputText class="kn-material-input p-m-2" :style="mainDescriptor.style.filterInput" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchItems" data-test="search-input" />
        <SelectButton id="model-select-buttons" v-model="tableMode" :options="selectButtonOptions" @click="getDatasetsByFilter" data-test="dataset-select" />
    </div>

    <div class="p-mx-2 kn-overflow">
        <DataTable v-if="!toggleCardDisplay" style="width:100%" class="p-datatable-sm kn-table" :value="filteredDatasets" :loading="loading" dataKey="objId" responsiveLayout="stack" breakpoint="600px" data-test="datasets-table">
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
                    <i v-if="slotProps.data.pars.length > 0 || slotProps.data.drivers.length > 0" class="fas fa-check p-button-link" />
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
            <Message v-if="filteredDatasets.length === 0" class="kn-flex p-m-2" severity="info" :closable="false" :style="mainDescriptor.style.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
            <template v-else>
                <WorkspaceCard
                    v-for="(dataset, index) of filteredDatasets"
                    :key="index"
                    :viewType="'dataset'"
                    :document="dataset"
                    :isPrepared="isAvroReady(dataset)"
                    @previewDataset="previewDataset"
                    @editFileDataset="editFileDataset"
                    @openDatasetInQBE="openDatasetInQBE($event)"
                    @exportToXlsx="exportDataset($event, 'xls')"
                    @exportToCsv="exportDataset($event, 'csv')"
                    @downloadDatasetFile="downloadDatasetFile"
                    @shareDataset="shareDataset"
                    @cloneDataset="cloneDataset"
                    @deleteDataset="deleteDatasetConfirm"
                    @prepareData="prepareData"
                    @openDataPreparation="openDataPreparation"
                    @openSidebar="showSidebar"
                />
            </template>
        </div>
    </div>

    <DetailSidebar
        :visible="showDetailSidebar"
        :viewType="'dataset'"
        :document="selectedDataset"
        :isPrepared="isAvroReady(selectedDataset)"
        :datasetCategories="datasetCategories"
        @previewDataset="previewDataset"
        @editFileDataset="editFileDataset"
        @openDatasetInQBE="openDatasetInQBE($event)"
        @exportToXlsx="exportDataset($event, 'xls')"
        @exportToCsv="exportDataset($event, 'csv')"
        @downloadDatasetFile="downloadDatasetFile"
        @shareDataset="shareDataset"
        @cloneDataset="cloneDataset"
        @deleteDataset="deleteDatasetConfirm"
        @prepareData="prepareData"
        @openDataPreparation="openDataPreparation"
        @close="showDetailSidebar = false"
        data-test="detail-sidebar"
    />

    <DatasetWizard v-if="showDatasetDialog" :selectedDataset="selectedDataset" :visible="showDatasetDialog" @closeDialog="showDatasetDialog = false" @closeDialogAndReload="closeWizardAndRealod" />
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
    <Menu id="creationMenu" ref="creationMenu" :model="creationMenuButtons" />

    <WorkspaceDataCloneDialog :visible="cloneDialogVisible" :propDataset="selectedDataset" @close="cloneDialogVisible = false" @clone="handleDatasetClone"></WorkspaceDataCloneDialog>
    <WorkspaceDataShareDialog :visible="shareDialogVisible" :propDataset="selectedDataset" :datasetCategories="datasetCategories" @close="shareDialogVisible = false" @share="handleDatasetShare"></WorkspaceDataShareDialog>
    <WorkspaceDataPreviewDialog v-if="previewDialogVisible" :visible="previewDialogVisible" :propDataset="selectedDataset" @close="previewDialogVisible = false" previewType="workspace"></WorkspaceDataPreviewDialog>
    <WorkspaceWarningDialog :visible="warningDialogVisbile" :title="$t('workspace.myData.title')" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceWarningDialog>

    <QBE v-if="qbeVisible" :visible="qbeVisible" :dataset="selectedQbeDataset" @close="closeQbe" />
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
import Message from 'primevue/message'
import WorkspaceDataCloneDialog from './dialogs/WorkspaceDataCloneDialog.vue'
import WorkspaceDataPreviewDialog from './dialogs/WorkspaceDataPreviewDialog.vue'
import WorkspaceDataShareDialog from './dialogs/WorkspaceDataShareDialog.vue'
import WorkspaceWarningDialog from '../../genericComponents/WorkspaceWarningDialog.vue'
import { AxiosResponse } from 'axios'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import SelectButton from 'primevue/selectbutton'
import QBE from '@/modules/qbe/QBE.vue'
import { Client } from '@stomp/stompjs'

export default defineComponent({
    components: { QBE, DataTable, Column, Chip, DetailSidebar, WorkspaceCard, Menu, KnFabButton, DatasetWizard, WorkspaceDataCloneDialog, WorkspaceWarningDialog, WorkspaceDataShareDialog, WorkspaceDataPreviewDialog, SelectButton, Message },
    emits: ['toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    computed: {
        isDatasetOwner(): any {
            return (this.$store.state as any).user.userId === this.selectedDataset.owner
        },
        showCkanIntegration(): any {
            return (this.$store.state as any).user.functionalities.indexOf('CkanIntegrationFunctionality') > -1
        },
        showQbeEditButton(): any {
            return (this.$store.state as any).user.userId === this.selectedDataset.owner && (this.selectedDataset.dsTypeCd == 'Federated' || this.selectedDataset.dsTypeCd == 'Qbe')
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
            datasetList: [] as any,
            filteredDatasets: [] as any,
            preparedDatasets: [] as any,
            datasetCategories: [] as any,
            selectedDataset: {} as any,
            menuButtons: [] as any,
            creationMenuButtons: [] as any,
            filters: {
                global: [filterDefault]
            } as Object,
            cloneDialogVisible: false,
            shareDialogVisible: false,
            previewDialogVisible: false,
            warningDialogVisbile: false,
            warningMessage: '',
            tableMode: 'My Datasets',
            selectButtonOptions: ['My Datasets', 'Enterprise', 'Shared', 'All Datasets'],
            searchWord: '' as string,
            qbeVisible: false,
            selectedQbeDataset: null
        }
    },
    async created() {
        await this.getAllData()
        await this.getAllPreparedData()
    },

    methods: {
        getDatasets(filter: string) {
            this.loading = true
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/${filter}/`)
        },
        async getAllPreparedData() {
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/prepared`)
                .then((response: AxiosResponse<any>) => {
                    this.preparedDatasets = response.data
                })
                .catch(() => {})
        },
        async getAllData() {
            await this.getDatasetsByFilter()
            await this.getDatasetCategories()
            // this.loading = false
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
                { key: '1', label: this.$t('workspace.myModels.openInQBE'), icon: 'fas fa-pen', command: () => this.openDatasetInQBE(), visible: this.showQbeEditButton },
                { key: '2', label: this.$t('workspace.myData.xlsxExport'), icon: 'fas fa-file-excel', command: () => this.exportDataset(clickedDocument, 'xls'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.selectedDataset.dsTypeCd != 'File' && this.datasetIsIterable },
                { key: '3', label: this.$t('workspace.myData.csvExport'), icon: 'fas fa-file-csv', command: () => this.exportDataset(clickedDocument, 'csv'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.selectedDataset.dsTypeCd != 'File' },
                { key: '4', label: this.$t('workspace.myData.fileDownload'), icon: 'fas fa-download', command: () => this.downloadDatasetFile(clickedDocument), visible: this.selectedDataset.dsTypeCd == 'File' },
                { key: '5', label: this.$t('workspace.myData.shareDataset'), icon: 'fas fa-share-alt', command: () => this.shareDataset(), visible: this.canLoadData && this.isDatasetOwner && this.selectedDataset.dsTypeCd != 'Prepared' },
                { key: '6', label: this.$t('workspace.myData.cloneDataset'), icon: 'fas fa-clone', command: () => this.cloneDataset(clickedDocument), visible: this.canLoadData && this.selectedDataset.dsTypeCd == 'Qbe' },
                { key: '7', label: this.$t('workspace.myData.prepareData'), icon: 'fas fa-cogs', command: () => this.prepareData(clickedDocument), visible: this.canLoadData && this.selectedDataset.dsTypeCd != 'Qbe' && this.selectedDataset.dsTypeCd != 'Prepared' },
                { key: '8', label: this.$t('workspace.myData.openDataPreparation'), icon: 'fas fa-cogs', command: () => this.openDataPreparation(clickedDocument), visible: (this.isAvroReady(this.selectedDataset) || this.selectedDataset.dsTypeCd == 'Prepared') && this.canLoadData && this.selectedDataset.dsTypeCd != 'Qbe' },
                { key: '9', label: this.$t('workspace.myData.deleteDataset'), icon: 'fas fa-trash', command: () => this.deleteDatasetConfirm(clickedDocument), visible: this.isDatasetOwner }
            )

        },
        createCreationMenuButtons() {
            this.creationMenuButtons = []
            this.creationMenuButtons.push(
                { key: '0', label: this.$t('managers.businessModelManager.uploadFile'), command: this.toggleDatasetDialog, visible: true },
                { key: '1', label: this.$t('workspace.myData.prepareData'), command: this.openDatasetInQBE, visible: true },
                { key: '2', label: this.$t('workspace.myData.openData'), command: this.openDatasetInQBE, visible: this.showCkanIntegration }
            )
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
        isAvroReady(dataset: any) {
            if (dataset && this.preparedDatasets.indexOf(dataset.label) >= 0) return true
            else return false
        },
        async prepareData(dataset: any) {
            if (dataset) {
                // launch avro export job
                this.$http
                    .post(
                        process.env.VUE_APP_RESTFUL_SERVICES_PATH + `/1.0/data-preparation/prepare/${dataset.id}`,
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
                            title: this.$t('workspace.myData.isPreparing')
                        })
                    })
                    .catch(() => {})

                // listen on websocket for avro export job to be finished
                let url = process.env.VUE_APP_HOST_URL.replace('http', 'ws') + '/knowage-data-preparation/ws?' + process.env.VUE_APP_DEFAULT_AUTH_HEADER + '=' + localStorage.getItem('token')
                let client = new Client({
                    brokerURL: url,
                    connectHeaders: {},
                    heartbeatIncoming: 4000,
                    heartbeatOutgoing: 4000
                })

                client.onConnect = (frame) => {
                    // Do something, all subscribes must be done is this callback
                    // This is needed because this will be executed after a (re)connect
                    console.log(frame)

                    client.subscribe(
                        '/user/queue/prepare',
                        (message) => {
                            // called when the client receives a STOMP message from the server
                            if (message.body) {
                                let avroJobResponse = JSON.parse(message.body)
                                if (avroJobResponse.statusOk) this.$store.commit('setInfo', { title: 'Dataset ' + avroJobResponse.dsLabel + ' prepared successfully' })
                                else this.$store.commit('setError', { title: 'Cannot prepare dataset ' + avroJobResponse.dsLabel, msg: avroJobResponse.errorMessage })

                                this.preparedDatasets.push(avroJobResponse.dsLabel)
                                client.deactivate()
                            } else {
                                this.$store.commit('setError', { title: 'Websocket error', msg: 'got empty message' })
                            }
                        },
                        {
                            dsLabel: dataset.label
                        }
                    )
                }

                client.onStompError = function(frame) {
                    // Will be invoked in case of error encountered at Broker
                    // Bad login/passcode typically will cause an error
                    // Complaint brokers will set `message` header with a brief message. Body may contain details.
                    // Compliant brokers will terminate the connection after any error
                    console.log('Broker reported error: ' + frame.headers['message'])
                    console.log('Additional details: ' + frame.body)
                }
                client.activate()
            }
        },
        openDataPreparation(dataset: any) {
            if (dataset.dsTypeCd == 'Prepared') {
                this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/advanced/${dataset.label}`).then(
                    (response: AxiosResponse<any>) => {
                        let instanceId = response.data.configuration.dataPrepInstanceId
                        this.$http.get(process.env.VUE_APP_DATA_PREPARATION_PATH + `1.0/process/by-instance-id/${instanceId}`).then(
                            (response: AxiosResponse<any>) => {
                                let transformations = response.data.definition
                                let datasetLabel = response.data.instances[0].dataSetLabel
                                this.$router.push({ name: 'data-preparation', params: { id: datasetLabel, transformations: JSON.stringify(transformations) } })
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
            } else {
                this.$router.push({ name: 'data-preparation', params: { id: dataset.label } })
            }
        },
        openDatasetInQBE() {
            this.$store.commit('setInfo', {
                title: 'Todo',
                msg: 'Functionality not in this sprint'
            })
        },
        async exportDataset(dataset: any, format: string) {
            this.loading = true

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
            await this.loadDataset(dataset.label)
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
                    this.getDatasetsByFilter()
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
                    this.getDatasetsByFilter()
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
                    this.getDatasetsByFilter()
                })
                .catch(() => {})
            this.loading = false
        },
        closeWarningDialog() {
            this.warningMessage = ''
            this.warningDialogVisbile = false
        },
        closeWizardAndRealod() {
            this.showDatasetDialog = false
            this.getDatasetsByFilter()
        },
        async getDatasetsByFilter() {
            this.searchWord = ''
            switch (this.tableMode) {
                case 'My Datasets':
                    this.datasetList = this.getDatasets('owned')
                        .then((response: AxiosResponse<any>) => {
                            this.datasetList = [...response.data.root]
                            this.filteredDatasets = [...this.datasetList]
                        })
                        .finally(() => (this.loading = false))
                    break
                case 'Enterprise':
                    this.datasetList = this.getDatasets('enterprise')
                        .then((response: AxiosResponse<any>) => {
                            this.datasetList = [...response.data.root]
                            this.filteredDatasets = [...this.datasetList]
                        })
                        .finally(() => (this.loading = false))
                    break
                case 'Shared':
                    this.datasetList = this.getDatasets('shared')
                        .then((response: AxiosResponse<any>) => {
                            this.datasetList = [...response.data.root]
                            this.filteredDatasets = [...this.datasetList]
                        })
                        .finally(() => (this.loading = false))
                    break
                case 'All Datasets':
                    this.datasetList = this.getDatasets('mydata')
                        .then((response: AxiosResponse<any>) => {
                            this.datasetList = [...response.data.root]
                            this.filteredDatasets = [...this.datasetList]
                        })
                        .finally(() => (this.loading = false))
            }
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredDatasets = [...this.datasetList] as any[]
                } else {
                    this.filteredDatasets = this.datasetList.filter((el: any) => {
                        return el.label?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.dsTypeCd?.toLowerCase().includes(this.searchWord.toLowerCase()) || this.datasetTagFound(el)
                    })
                }
            }, 250)
        },
        closeQbe() {
            this.qbeVisible = false
            this.selectedQbeDataset = null
        },
        datasetTagFound(dataset: any) {
            let tagFound = false
            for (let i = 0; i < dataset.tags.length; i++) {
                const tempTag = dataset.tags[i]
                if (tempTag.name.toLowerCase() === this.searchWord.toLowerCase()) {
                    tagFound = true
                    break
                }
            }
            return tagFound
        }
    }
})
</script>

<style lang="scss" scoped>
#model-select-buttons {
    margin: 2rem 2rem 2rem auto;
}
</style>
