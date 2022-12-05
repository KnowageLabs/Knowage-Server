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

    <div class="p-d-flex p-flex-row p-ai-center p-flex-wrap">
        <InputText class="kn-material-input p-m-3 model-search" :style="mainDescriptor.style.filterInput" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchItems" data-test="search-input" />
        <span class="p-float-label p-mr-auto model-search">
            <MultiSelect class="kn-material-input kn-width-full" :style="mainDescriptor.style.multiselect" v-model="selectedCategories" :options="datasetCategories" optionLabel="VALUE_CD" @change="searchItems" :filter="true" />
            <label class="kn-material-input-label"> {{ $t('common.type') }} </label>
        </span>
        <SelectButton class="p-mx-2" v-model="tableMode" :options="selectButtonOptions" @click="getDatasetsByFilter" data-test="dataset-select" />
    </div>

    <div class="kn-overflow">
        <DataTable v-if="!toggleCardDisplay" class="p-datatable-sm kn-table p-mx-2" :value="filteredDatasets" :loading="loading" dataKey="objId" responsiveLayout="stack" breakpoint="600px" data-test="datasets-table">
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
                    :isAvroReady="isAvroReady(dataset.id)"
                    @previewDataset="previewDataset"
                    @editDataset="editDataset"
                    @openDatasetInQBE="openDatasetInQBE($event)"
                    @exportToXlsx="prepareDatasetForExport($event, 'xls')"
                    @exportToCsv="prepareDatasetForExport($event, 'csv')"
                    @downloadDatasetFile="downloadDatasetFile"
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
        :isAvroReady="isAvroReady(selectedDataset.id)"
        :datasetCategories="datasetCategories"
        @previewDataset="previewDataset"
        @editDataset="editDataset"
        @openDatasetInQBE="openDatasetInQBE($event)"
        @exportToXlsx="prepareDatasetForExport($event, 'xls')"
        @exportToCsv="prepareDatasetForExport($event, 'csv')"
        @downloadDatasetFile="downloadDatasetFile"
        @shareDataset="shareDataset"
        @cloneDataset="cloneDataset"
        @deleteDataset="deleteDatasetConfirm"
        @openDataPreparation="openDataPreparation"
        @close="showDetailSidebar = false"
        data-test="detail-sidebar"
        @monitoring="showMonitoring = !showMonitoring"
    />

    <div v-if="parameterSidebarVisible" id="document-execution-backdrop" @click="parameterSidebarVisible = false"></div>
    <KnParameterSidebar
        v-if="parameterSidebarVisible"
        style="height: 100%; top: 0 !important"
        class="workspace-parameter-sidebar kn-overflow-y"
        :filtersData="filtersData"
        :propDocument="selectedDataset"
        :propMode="'workspaceView'"
        :propQBEParameters="selectedDataset.pars"
        :userRole="userRole"
        :dataset="selectedDataset"
        @execute="onExecute"
        @roleChanged="onRoleChange"
    />

    <DatasetWizard v-if="showDatasetDialog" :selectedDataset="selectedDataset" :visible="showDatasetDialog" @closeDialog="showDatasetDialog = false" @closeDialogAndReload="closeWizardAndRealod" />
    <EditPreparedDatasetDialog :dataset="selectedDataset" :visible="showEditPreparedDatasetDialog" @save="updatePreparedDataset" @cancel="showEditPreparedDatasetDialog = false" />
    <TieredMenu class="kn-tieredMenu" id="optionsMenu" ref="optionsMenu" :model="menuButtons" :popup="true" />
    <Menu id="creationMenu" ref="creationMenu" :model="creationMenuButtons" />

    <WorkspaceDataCloneDialog :visible="cloneDialogVisible" :propDataset="selectedDataset" @close="cloneDialogVisible = false" @clone="handleDatasetClone"></WorkspaceDataCloneDialog>
    <WorkspaceDataShareDialog :visible="shareDialogVisible" :propDataset="selectedDataset" :datasetCategories="datasetCategories" @close="shareDialogVisible = false" @share="handleDatasetShare"></WorkspaceDataShareDialog>
    <WorkspaceDataPreviewDialog v-if="previewDialogVisible" :visible="previewDialogVisible" :propDataset="selectedDataset" @close="previewDialogVisible = false" previewType="workspace"></WorkspaceDataPreviewDialog>
    <WorkspaceWarningDialog :visible="warningDialogVisbile" :title="$t('workspace.myData.title')" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceWarningDialog>

    <DataPreparationAvroHandlingDialog :visible="dataPrepAvroHandlingDialogVisbile" :title="$t('workspace.myData.isPreparing')" :infoMessage="dataPrepAvroHandlingMessage" @close="proceedToDataPrep" :events="events"></DataPreparationAvroHandlingDialog>

    <QBE v-if="qbeVisible" :visible="qbeVisible" :dataset="selectedQbeDataset" @close="closeQbe" />
    <DataPreparationMonitoringDialog v-model:visibility="showMonitoring" @close="showMonitoring = false" @save="updateDatasetWithNewCronExpression" :dataset="selectedDataset"></DataPreparationMonitoringDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import DatasetWizard from './datasetWizard/WorkspaceDatasetWizardContainer.vue'
import EditPreparedDatasetDialog from './dialogs/EditPreparedDatasetDialog.vue'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Chip from 'primevue/chip'
import Menu from 'primevue/contextmenu'
import TieredMenu from 'primevue/tieredmenu'
import Message from 'primevue/message'
import WorkspaceDataCloneDialog from './dialogs/WorkspaceDataCloneDialog.vue'
import WorkspaceDataPreviewDialog from './dialogs/WorkspaceDataPreviewDialog.vue'
import WorkspaceDataShareDialog from './dialogs/WorkspaceDataShareDialog.vue'
import WorkspaceWarningDialog from '../../genericComponents/WorkspaceWarningDialog.vue'
import DataPreparationAvroHandlingDialog from '@/modules/workspace/dataPreparation/DataPreparationAvroHandlingDialog.vue'
import { AxiosResponse } from 'axios'
import { downloadDirectFromResponseWithCustomName } from '@/helpers/commons/fileHelper'
import SelectButton from 'primevue/selectbutton'
import QBE from '@/modules/qbe/QBE.vue'
import DataPreparationMonitoringDialog from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoringDialog.vue'
import MultiSelect from 'primevue/multiselect'
import moment from 'moment'
import KnParameterSidebar from '@/components/UI/KnParameterSidebar/KnParameterSidebar.vue'
import { mapState, mapActions } from 'pinia'

import mainStore from '../../../../App.store'
import workspaceStore from '@/modules/workspace/Workspace.store.js'
import { Client } from '@stomp/stompjs'
import { getCorrectRolesForExecution } from '@/helpers/commons/roleHelper'

export default defineComponent({
    components: {
        QBE,
        MultiSelect,
        DataTable,
        Column,
        Chip,
        DataPreparationMonitoringDialog,
        EditPreparedDatasetDialog,
        DetailSidebar,
        WorkspaceCard,
        Menu,
        KnFabButton,
        DatasetWizard,
        DataPreparationAvroHandlingDialog,
        WorkspaceDataCloneDialog,
        WorkspaceWarningDialog,
        WorkspaceDataShareDialog,
        WorkspaceDataPreviewDialog,
        SelectButton,
        Message,
        KnParameterSidebar,
        TieredMenu
    },
    emits: ['toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    computed: {
        ...mapState(mainStore, ['user']),
        ...mapState(workspaceStore, ['dataPreparation', 'isAvroReady']),
        isDatasetOwner(): any {
            return this.user.userId === this.selectedDataset.owner
        },
        showCkanIntegration(): any {
            return this.user.functionalities.indexOf('CkanIntegrationFunctionality') > -1
        },
        showQbeEditButton(): any {
            return this.user.userId === this.selectedDataset.owner && (this.selectedDataset.dsTypeCd == 'Federated' || this.selectedDataset.dsTypeCd == 'Qbe')
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
            showEditPreparedDatasetDialog: false,
            datasetList: [] as any,
            selectedCategories: [] as any,
            selectedCategoryIds: [] as any,
            filteredDatasets: [] as any,
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
            client: {} as any,
            selectedQbeDataset: null,
            showMonitoring: false,
            filtersData: {} as any,
            userRole: null,
            parameterSidebarVisible: false,
            exportFormat: '',
            dataPrepAvroHandlingDialogVisbile: false,
            dataPrepAvroHandlingMessage: '',
            events: [] as any
        }
    },
    async created() {
        this.userRole = this.user.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.user.sessionRole : null
        await this.getAllData()

        if (this.user?.functionalities.includes('DataPreparation')) {
            this.events = []
            var url = new URL(window.location.origin)
            url.protocol = url.protocol.replace('http', 'ws')
            let uri = url + 'knowage-data-preparation/ws?' + import.meta.env.VITE_DEFAULT_AUTH_HEADER + '=' + localStorage.getItem('token')
            this.client = new Client({
                brokerURL: uri,
                connectHeaders: {},
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000
            })

            this.client.onConnect = (frame) => {
                // Do something, all subscribes must be done is this callback
                // This is needed because this will be executed after a (re)connect
                console.log(frame)

                this.client.subscribe('/user/queue/prepare', (message) => {
                    // called when the client receives a STOMP message from the server
                    if (message.body) {
                        let avroJobResponse = JSON.parse(message.body)
                        if (avroJobResponse.statusOk) {
                            this.addToLoadedAvros(avroJobResponse.dsId)
                            this.addToAvroDatasets(avroJobResponse.dsId)

                            if (this.dataPrepAvroHandlingDialogVisbile) {
                                this.pushEvent(4)
                            } else {
                                this.setInfo({ title: this.$t('managers.workspaceManagement.dataPreparation.dataPreparationIsCompleted') })
                                setTimeout(() => {
                                    this.openDataPreparation({ id: avroJobResponse.dsId })
                                }, 1500)
                            }
                        } else {
                            if (this.dataPrepAvroHandlingDialogVisbile) {
                                this.pushEvent(5)
                            } else {
                                this.setError({ title: 'Cannot prepare dataset', msg: avroJobResponse.errorMessage })
                            }
                            this.removeFromLoadingAvros(avroJobResponse.dsId)
                        }
                    } else {
                        this.setError({ title: 'Websocket error', msg: 'got empty message' })
                    }
                })
            }

            this.client.onStompError = function (frame) {
                // Will be invoked in case of error encountered at Broker
                // Bad login/passcode typically will cause an error
                // Complaint brokers will set `message` header with a brief message. Body may contain details.
                // Compliant brokers will terminate the connection after any error
                console.log('Broker reported error: ' + frame.headers['message'])
                console.log('Additional details: ' + frame.body)
            }
            this.client.activate()
        }
    },

    methods: {
        ...mapActions(mainStore, ['setInfo', 'setError']),
        ...mapActions(workspaceStore, ['addToLoadedAvros', 'addToLoadingAvros', 'addToAvroDatasets', 'removeFromLoadingAvros', 'removeFromLoadedAvros', 'setAvroDatasets', 'setLoadedAvros', 'setLoadingAvros']),
        async updatePreparedDataset(newDataset) {
            this.showEditPreparedDatasetDialog = false
            this.selectedDataset.name = newDataset.name
            this.selectedDataset.description = newDataset.description
            this.selectedDataset.type = 'PreparedDataset'

            await this.$http({
                method: 'POST',
                url: import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'selfservicedataset/update',
                data: this.selectedDataset,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Disable-Errors': 'true' },

                transformRequest: function (obj) {
                    var str = [] as any
                    for (var p in obj) str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]))
                    return str.join('&')
                }
            })
                .then(() => {
                    this.setInfo({ title: 'Updated successfully' })
                })
                .catch(() => {
                    this.setError({ title: 'Save error', msg: 'Cannot update Prepared Dataset' })
                })
            await this.getAllData()
        },

        getDatasets(filter: string) {
            this.loading = true
            return this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/datasets/${filter}/`)
        },
        async getAllAvroDataSets() {
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/datasets/avro`)
                .then((response: AxiosResponse<any>) => {
                    this.setAvroDatasets(response.data)
                    this.setLoadedAvros(response.data)
                    this.setLoadingAvros([])
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
            return this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `domainsforfinaluser/ds-categories`).then((response: AxiosResponse<any>) => {
                this.datasetCategories = [...response.data]
            })
        },
        async loadDataset(datasetLabel: string) {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/datasets/${datasetLabel}`)
                .then((response: AxiosResponse<any>) => {
                    this.selectedDataset = response.data[0]
                })
                .catch(() => {})
            this.loading = false
        },
        async loadDatasetDrivers(dataset) {
            let hasError = false
            if (dataset.label && dataset.id && dataset.dsTypeCd !== 'Prepared') {
                let postPayload = { role: this.userRole || this.user.roles[0] }
                await this.$http
                    .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/datasets/${dataset.label}/filters`, postPayload)
                    .then((response: AxiosResponse<any>) => {
                        this.filtersData = response.data
                        if (this.filtersData.filterStatus) {
                            this.filtersData.filterStatus = this.filtersData.filterStatus.filter((filter: any) => filter.id)
                        }
                    })
                    .catch(() => {
                        hasError = true
                    })
                await this.formatDrivers()
            }
            return hasError
        },
        formatDrivers() {
            this.filtersData?.filterStatus?.forEach((el: any) => {
                el.parameterValue = el.multivalue ? [] : [{ value: '', description: '' }]
                if (el.driverDefaultValue?.length > 0) {
                    let valueIndex = '_col0'
                    let descriptionIndex = 'col1'
                    if (el.metadata?.colsMap) {
                        valueIndex = Object.keys(el.metadata?.colsMap).find((key: string) => el.metadata.colsMap[key] === el.metadata.valueColumn) as any
                        descriptionIndex = Object.keys(el.metadata?.colsMap).find((key: string) => el.metadata.colsMap[key] === el.metadata.descriptionColumn) as any
                    }
                    el.parameterValue = el.driverDefaultValue.map((defaultValue: any) => {
                        return { value: defaultValue.value ?? defaultValue[valueIndex], description: defaultValue.desc ?? defaultValue[descriptionIndex] }
                    })
                    if (el.type === 'DATE' && !el.selectionType && el.valueSelection === 'man_in' && el.showOnPanel === 'true') {
                        el.parameterValue[0].value = moment(el.parameterValue[0].description?.split('#')[0]).toDate() as any
                    }
                }
                if (el.data) {
                    el.data = el.data.map((data: any) => {
                        return this.formatParameterDataOptions(el, data)
                    })
                    if (el.data.length === 1) {
                        el.parameterValue = [...el.data]
                    }
                }
                if ((el.selectionType === 'COMBOBOX' || el.selectionType === 'LIST') && el.multivalue && el.mandatory && el.data.length === 1) {
                    el.showOnPanel = 'false'
                }
                if (!el.parameterValue) {
                    el.parameterValue = [{ value: '', description: '' }]
                }
                if (el.parameterValue[0] && !el.parameterValue[0].description) {
                    el.parameterValue[0].description = el.parameterDescription ? el.parameterDescription[0] : ''
                }
            })
        },
        formatParameterDataOptions(parameter: any, data: any) {
            const valueColumn = parameter.metadata.valueColumn
            const descriptionColumn = parameter.metadata.descriptionColumn
            const valueIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === valueColumn)
            const descriptionIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === descriptionColumn)
            return { value: valueIndex ? data[valueIndex] : '', description: descriptionIndex ? data[descriptionIndex] : '' }
        },
        getFormattedDrivers(parameters) {
            let formattedParameters = {} as any
            Object.keys(parameters.filterStatus).forEach((key: any) => {
                const parameter = parameters.filterStatus[key]
                if (!parameter.multivalue) {
                    // formattedParameters.push({ label: parameter.label, value: parameter.parameterValue[0].value, description: parameter.parameterValue[0].description ?? '' })
                    formattedParameters[parameter.urlName] = []
                    formattedParameters[parameter.urlName].push({ value: parameter.parameterValue[0].value, description: parameter.parameterValue[0].description ?? parameter.parameterValue[0].value })
                } else {
                    // formattedParameters.push({ label: parameter.label, value: parameter.parameterValue?.map((el: any) => el.value), description: parameter.parameterDescription ?? '' })
                    formattedParameters[parameter.urlName] = []
                    formattedParameters[parameter.urlName].push({ value: parameter.parameterValue?.map((el: any) => el.value), description: parameter.parameterDescription ?? parameter.parameterValue[0].value })
                }
            })
            return formattedParameters
        },
        async onExecute(parameters, drivers) {
            const postData = { drivers: this.getFormattedDrivers(drivers), parameters: parameters }
            this.exportDataset(postData)
        },
        async prepareDatasetForExport(dataset: any, format: string) {
            this.exportFormat = format
            await this.loadDataset(dataset.label)
            await this.loadDatasetDrivers(this.selectedDataset)
            if (this.selectedDataset.pars.length > 0 || this.filtersData.filterStatus.length > 0) {
                this.parameterSidebarVisible = true
            } else this.exportDataset()
        },
        async exportDataset(postData?) {
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/export/dataset/${this.selectedDataset.id}/${this.exportFormat}`, postData ?? {}, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then(() => {
                    this.setInfo({
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('workspace.myData.exportSuccess')
                    })
                })
                .catch(() => {})
                .finally(() => (this.parameterSidebarVisible = false))
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

        let tmp = [] as any
        tmp.push(
            { key: '0', label: this.$t('workspace.myModels.editDataset'), icon: 'fas fa-pen', command: this.editDataset, visible: this.isDatasetOwner && (this.selectedDataset.dsTypeCd == 'File' || this.selectedDataset.dsTypeCd == 'Prepared') },
            { key: '1', label: this.$t('workspace.myModels.editDataset'), icon: 'fas fa-pen', command: () => this.openDatasetInQBE(clickedDocument), visible: this.showQbeEditButton },
            {
                key:'100',
                label:this.$t('common.export'),
                icon:'pi pi-fw pi-plus',
                visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.selectedDataset.dsTypeCd != 'File',
                items:[
                {
                    key: '2', label: this.$t('workspace.myData.xlsxExport'), icon: 'fas fa-file-excel', command: () => this.prepareDatasetForExport(clickedDocument, 'xls'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.selectedDataset.dsTypeCd != 'File' && this.datasetIsIterable },
                    { key: '3', label: this.$t('workspace.myData.csvExport'), icon: 'fas fa-file-csv', command: () => this.prepareDatasetForExport(clickedDocument, 'csv'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.selectedDataset.dsTypeCd != 'File' },
                ]
            },
            { key: '4', label: this.$t('workspace.myData.fileDownload'), icon: 'fas fa-download', command: () => this.downloadDatasetFile(clickedDocument), visible: this.selectedDataset.dsTypeCd == 'File' },
            { key: '5', label: this.$t('workspace.myData.shareDataset'), icon: 'fas fa-share-alt', command: () => this.shareDataset(), visible: this.canLoadData && this.isDatasetOwner && this.selectedDataset.dsTypeCd != 'Prepared' },
            { key: '6', label: this.$t('workspace.myData.cloneDataset'), icon: 'fas fa-clone', command: () => this.cloneDataset(clickedDocument), visible: this.canLoadData && this.selectedDataset.dsTypeCd == 'Qbe' },

            { key: '9', label: this.$t('workspace.myData.deleteDataset'), icon: 'fas fa-trash', command: () => this.deleteDatasetConfirm(clickedDocument), visible: this.isDatasetOwner },
            { key: '10', label: this.$t('workspace.myModels.openInQBE'), icon: 'fas fa-pen', command: () => this.openQBEUponDataset(clickedDocument), visible: this.selectedDataset?.isPersisted }
        )

        if (this.user?.functionalities.includes('DataPreparation')) {
            tmp.push({ key: '7', label: this.$t('workspace.myData.openDataPreparation'), icon: 'fas fa-cogs', command: () => this.openDataPreparation(clickedDocument), visible: this.canLoadData && (this.selectedDataset.pars && this.selectedDataset.pars.length == 0) })
        }

        tmp = tmp.sort((a,b)=>a.key.localeCompare(b.key))

        this.menuButtons = tmp

    },
        createCreationMenuButtons() {
            this.creationMenuButtons = []
            this.creationMenuButtons.push({ key: '0', label: this.$t('managers.businessModelManager.uploadFile'), command: this.toggleDatasetDialog, visible: true }, { key: '1', label: this.$t('workspace.myData.openData'), command: this.openDatasetInQBE, visible: this.showCkanIntegration })
        },
        toggleDatasetDialog() {
            this.selectedDataset = {}
            this.showDatasetDialog = true
        },
        async previewDataset(dataset: any) {
            await this.loadDataset(dataset.label)
            getCorrectRolesForExecution(null, dataset).then(async () => {
                if (this.selectedDataset) this.selectedDataset.drivers = dataset.drivers
                this.previewDialogVisible = true
            })
        },
        editDataset() {
            if (this.selectedDataset.dsTypeCd == 'File') this.showDatasetDialog = true
            else if (this.selectedDataset.dsTypeCd == 'Prepared') this.showEditPreparedDatasetDialog = true
        },

        async generateAvro(dsId: Number) {
            this.pushEvent(1)
            // launch avro export job
            this.$http
                .post(
                    import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/data-preparation/prepare/${dsId}`,
                    {},
                    {
                        headers: {
                            Accept: 'application/json, text/plain, */*',
                            'Content-Type': 'application/json;charset=UTF-8'
                        }
                    }
                )
                .then(() => {
                    this.pushEvent(2)
                    this.addToLoadingAvros(dsId)
                    let idx = this.dataPreparation.loadedAvros.indexOf(dsId)
                    if (idx >= 0) this.removeFromLoadedAvros(idx)
                    this.pushEvent(3)
                })
                .catch(() => {})

            // listen on websocket for avro export job to be finished
            if (this.user?.functionalities.includes('DataPreparation') && Object.keys(this.client).length > 0) this.client.publish({ destination: '/app/prepare', body: dsId })
        },
        async openDataPreparation(dataset: any) {
            this.events = []
            this.pushEvent(0)
            if (dataset?.dsTypeCd == 'Prepared') {
                //edit existing data prep
                this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/datasets/advanced/${dataset.id}`).then(
                    (response: AxiosResponse<any>) => {
                        let instanceId = response.data.configuration.dataPrepInstanceId
                        this.$http.get(import.meta.env.VITE_DATA_PREPARATION_PATH + `1.0/process/by-instance-id/${instanceId}`).then(
                            (response: AxiosResponse<any>) => {
                                let transformations = response.data.definition
                                let processId = response.data.id
                                let datasetId = response.data.instance.dataSetId
                                if (!this.isAvroReady(datasetId)) {
                                    // check if Avro file has been deleted or not
                                    /*                                 this.$router.push({ name: 'data-preparation', params: { id: datasetId, transformations: JSON.stringify(transformations), processId: processId, instanceId: instanceId, dataset: JSON.stringify(dataset) } }) */

                                    this.dataPrepAvroHandlingDialogVisbile = true
                                    this.dataPrepAvroHandlingMessage = this.$t('managers.workspaceManagement.dataPreparation.info.dataPrepIsLoadingAndWillBeOpened')

                                    this.generateAvro(datasetId)
                                } else {
                                    this.$router.push({ name: 'data-preparation', params: { id: datasetId, transformations: JSON.stringify(transformations), processId: processId, instanceId: instanceId, dataset: JSON.stringify(dataset) } })
                                }
                            },
                            () => {
                                this.setError({ title: 'Save error', msg: 'Cannot create process' })
                            }
                        )
                    },
                    () => {
                        this.setError({
                            title: 'Cannot open data preparation'
                        })
                    }
                )
            } else if (this.isAvroReady(dataset.id)) {
                // original dataset already exported in Avro
                this.$router.push({ name: 'data-preparation', params: { id: dataset.id } })
            } else {
                this.dataPrepAvroHandlingDialogVisbile = true
                this.dataPrepAvroHandlingMessage = this.$t('managers.workspaceManagement.dataPreparation.info.dataPrepIsLoadingAndWillBeOpened')
                await this.generateAvro(dataset.id)
            }
        },
        openDatasetInQBE(dataset: any) {
            this.selectedQbeDataset = dataset
            this.qbeVisible = true
        },
        openQBEUponDataset(dataset: any) {
            this.selectedDataset = dataset
            this.qbeVisible = true
        },
        async downloadDatasetFile(dataset: any) {
            await this.loadDataset(dataset.label)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/download/file?dsLabel=${this.selectedDataset.label}&type=${this.selectedDataset.fileType.toLowerCase()}`, {
                    headers: {
                        Accept: 'application/json, text/plain, */*'
                    },
                    responseType: 'blob'
                })
                .then((response: AxiosResponse<any>) => {
                    if (response.data.errors) {
                        this.setError({
                            title: this.$t('common.error.downloading'),
                            msg: this.$t('common.error.downloading')
                        })
                    } else {
                        let fileName = response.headers['content-disposition'].split('fileName=')[1].split(';')[0]
                        downloadDirectFromResponseWithCustomName(response, fileName)
                        this.setInfo({ title: this.$t('common.toast.success') })
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
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url)
                .then(() => {
                    this.setInfo({
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
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/datasets`, dataset, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.setInfo({
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
            this.$router.push({ name: 'data-preparation', params: { id: dataset.id } })
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
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/datasets/${dataset.label}`)
                .then(() => {
                    this.setInfo({
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
        proceedToDataPrep() {
            this.dataPrepAvroHandlingMessage = ''
            this.dataPrepAvroHandlingDialogVisbile = false

            if (this.isAvroReady(this.selectedDataset.id)) this.openDataPreparation(this.selectedDataset)
        },
        closeWizardAndRealod() {
            this.showDatasetDialog = false
            this.getDatasetsByFilter()
        },
        async getDatasetsByFilter() {
            await this.getAllAvroDataSets()
            this.searchWord = ''
            this.selectedCategoryIds = [] as any
            this.selectedCategories = [] as any
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
        searchItems(event?) {
            setTimeout(() => {
                if (event.value) {
                    this.selectedCategoryIds = [] as any
                    event.value.forEach((el) => {
                        this.selectedCategoryIds.push(el.VALUE_ID)
                    })
                }
                if (!this.searchWord.trim().length && this.selectedCategoryIds.length == 0) {
                    this.filteredDatasets = [...this.datasetList] as any[]
                } else if (this.selectedCategoryIds.length > 0) {
                    this.filteredDatasets = this.datasetList.filter((el: any) => {
                        return (
                            this.selectedCategoryIds.includes(el.catTypeId) &&
                            (el.label?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.dsTypeCd?.toLowerCase().includes(this.searchWord.toLowerCase()) || this.datasetTagFound(el))
                        )
                    })
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
                if (tempTag.name.toLowerCase().includes(this.searchWord.toLowerCase())) {
                    tagFound = true
                    break
                }
            }
            return tagFound
        },
        handleMonitoring(selectedDataset) {
            this.selectedDataset = selectedDataset
            this.showMonitoring = !this.showMonitoring
        },
        async updateDatasetWithNewCronExpression(newCron) {
            this.showMonitoring = false

            await this.$http.post(import.meta.env.VITE_DATA_PREPARATION_PATH + '1.0/process', newCron).then(
                () => {
                    this.loadDataset(this.selectedDataset.label)
                    this.setInfo({ title: this.$t('common.save'), msg: this.$t('common.toast.updateSuccess') })
                },
                () => {
                    this.setError({ title: this.$t('common.error.saving'), msg: this.$t('managers.workspaceManagement.dataPreparation.errors.updatingSchedulation') })
                }
            )
        },
        pushEvent(id: Number) {
            let message = {}
            switch (id) {
                case 0:
                    message = { id: 0, message: this.$t('managers.workspaceManagement.dataPreparation.dataPreparationIsStarting') }
                    break
                case 1:
                    message = { id: 1, message: this.$t('managers.workspaceManagement.dataPreparation.dataPreparationIsManagingTheDataset') }
                    break
                case 2:
                    message = { id: 2, message: this.$t('managers.workspaceManagement.dataPreparation.dataPreparationIsCreatingFiles') }
                    break
                case 3:
                    message = { id: 3, message: this.$t('managers.workspaceManagement.dataPreparation.dataPreparationIsApplyingTheChanges') }
                    break
                case 4:
                    message = { id: 4, message: this.$t('managers.workspaceManagement.dataPreparation.dataPreparationIsCompleted') }
                    break
                case 5:
                    message = { id: 4, message: this.$t('managers.workspaceManagement.dataPreparation.dataPreparationStoppedWithErrors'), status: 'error' }
                    break
            }
            setTimeout(this.events.push(message), 1500)
        }
    },
    unmounted() {
        if (this.user?.functionalities.includes('DataPreparation') && this.client && Object.keys(this.client).length > 0) {
            this.client.deactivate()
            this.client = {}
        }
    }
})
</script>
<style lang="scss" scoped>
.model-search {
    flex: 0.3;
}
#document-execution-backdrop {
    background-color: rgba(33, 33, 33, 1);
    opacity: 0.48;
    z-index: 50;
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}
</style>
