<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary" style="width:100%">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.myData.title') }}
        </template>
        <template #right>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
        </template>
    </Toolbar>

    <InputText class="kn-material-input p-m-2" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />
    <ProgressBar mode="indeterminate" class="kn-progress-bar p-ml-2" v-if="loading" data-test="progress-bar" />

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
                @deleteDataset="deleteDataset"
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
        @deleteDataset="deleteDataset"
        @close="showDetailSidebar = false"
    />

    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Chip from 'primevue/chip'
import Menu from 'primevue/contextmenu'

export default defineComponent({
    components: { DataTable, Column, Chip, DetailSidebar, WorkspaceCard, Menu },
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
                for (var i = 0; i < this.selectedDataset.actions.length; i++) {
                    var action = this.selectedDataset.actions[i]
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
            allDataset: [] as any,
            datasetCategories: [] as any,
            selectedDataset: {} as any,
            menuButtons: [] as any,
            filters: {
                global: [filterDefault]
            } as Object
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
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/mydata/`).then((response) => {
                this.allDataset = [...response.data.root]
            })
        },
        async getDatasetCategories() {
            this.loading = true
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domainsforfinaluser/ds-categories`).then((response) => {
                this.datasetCategories = [...response.data]
            })
        },
        toggleDisplayView() {
            this.$emit('toggleDisplayView')
        },
        showSidebar(clickedDataset) {
            this.selectedDataset = clickedDataset
            this.showDetailSidebar = true
        },
        showMenu(event, clickedDocument) {
            this.selectedDataset = clickedDocument
            this.createMenuItems()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        // prettier-ignore
        createMenuItems() {
            this.menuButtons = []

                this.menuButtons.push(
                    { key: '0', label: this.$t('workspace.myAnalysis.menuItems.showDsDetails'), icon: 'fas fa-pen', command: this.editFileDataset, visible: this.isDatasetOwner && this.selectedDataset.dsTypeCd == 'File' },
                    { key: '1', label: this.$t('workspace.myModels.openInQBE'), icon: 'fas fa-pen', command: this.openDatasetInQBE, visible: this.showQbeEditButton },
                    { key: '2', label: this.$t('workspace.myData.xlsxExport'), icon: 'fas fa-file-excel', command: this.exportToXlsx, visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.selectedDataset.dsTypeCd != 'File' && this.datasetIsIterable },
                    { key: '3', label: this.$t('workspace.myData.csvExport'), icon: 'fas fa-file-csv', command: this.exportToCsv, visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.selectedDataset.dsTypeCd != 'File' },
                    { key: '4', label: this.$t('workspace.myData.fileDownload'), icon: 'fas fa-download', command: this.downloadDatasetFile, visible: this.selectedDataset.dsTypeCd == 'File' },
                    { key: '5', label: this.$t('workspace.myData.shareDataset'), icon: 'fas fa-share-alt', command: this.shareDataset, visible: this.canLoadData && this.isDatasetOwner },
                    { key: '6', label: this.$t('workspace.myData.cloneDataset'), icon: 'fas fa-clone', command: this.cloneDataset, visible: this.canLoadData && this.selectedDataset.dsTypeCd == 'Qbe' },
                    { key: '7', label: this.$t('workspace.myData.deleteDataset'), icon: 'fas fa-trash', command: this.deleteDataset, visible: this.isDatasetOwner }
                )
            
        },
        previewDataset(event) {
            console.log('previewDataset(event) {', event)
        },
        editFileDataset(event) {
            console.log('editFileDataset(event) {', event)
        },
        openDatasetInQBE(event) {
            console.log('openDatasetInQBE(event) {', event)
        },
        exportToXlsx(event) {
            console.log('exportToXlsx(event) {', event)
        },
        exportToCsv(event) {
            console.log('exportToCsv(event) {', event)
        },
        downloadDatasetFile(event) {
            console.log('downloadDatasetFile(event) {', event)
        },
        shareDataset(event) {
            console.log('shareDataset(event) {', event)
        },
        cloneDataset(event) {
            console.log('shareDcloneDatasetataset(event) {', event)
        },
        deleteDataset(event) {
            console.log('deleteDataset(event) {', event)
        }
    }
})
</script>
