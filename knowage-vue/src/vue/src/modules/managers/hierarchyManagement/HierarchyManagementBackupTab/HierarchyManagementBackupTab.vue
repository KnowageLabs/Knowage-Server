<template>
    <div class="p-d-flex p-flex-row">
        <Card class="p-m-2 p-d-flex p-flex-column hierarchy-scrollable-card">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #start>
                        <i class="fa fa-list p-mr-2"></i>
                        <span>{{ $t('managers.hierarchyManagement.backup') }}</span>
                    </template>
                </Toolbar>
            </template>

            <template #content>
                <form class="p-fluid p-formgrid p-grid">
                    <div class="p-field p-col-12 p-lg-4">
                        <span class="p-float-label">
                            <Dropdown v-model="selectedDimension" class="kn-material-input" :options="dimensions" option-label="DIMENSION_NM" @change="onSelectedDimensionChange" />
                            <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.dimensions') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-12 p-lg-4">
                        <span class="p-float-label">
                            <Dropdown v-model="hierarchyType" class="kn-material-input" :options="hierarchyManagementHierarchiesCardDescriptor.hierarchyTypes" :disabled="!selectedDimension" @change="onHierarchyTypeSelected" />
                            <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchyType') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-12 p-lg-4">
                        <span class="p-float-label">
                            <Dropdown v-model="selectedHierarchy" class="kn-material-input" :options="hierarchies" option-label="HIER_NM" :disabled="!hierarchyType" @change="onHierarchySelected" />
                            <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchies') }} </label>
                        </span>
                    </div>
                </form>

                <DataTable v-model:editing-rows="editingRows" v-model:filters="filters" :value="rows" class="p-datatable-sm kn-table" edit-mode="row" :global-filter-fields="globalFilterFields" :loading="loading" responsive-layout="stack" breakpoint="700px" @row-edit-save="updateBackupConfirm">
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <template #header>
                        <div class="table-header p-d-flex">
                            <span class="p-input-icon-left p-mr-3 p-col-12">
                                <i class="pi pi-search" />
                                <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" />
                            </span>
                        </div>
                    </template>
                    <Column v-for="column in columns" :key="column.field" class="kn-truncated" :header="$t(column.header)" :sort-field="column.field" :sortable="true">
                        <template #body="slotProps">
                            <span v-tooltip.top="slotProps.data[column.field]" class="kn-cursor-pointer"> {{ slotProps.data[column.field] }}</span>
                        </template>
                        <template #editor="slotProps">
                            <InputText v-if="column.header == 'Name' || column.header == 'Description'" v-model="slotProps.data[column.field]" v-tooltip.top="slotProps.data[column.field]" class="kn-material-input" />
                            <span v-else v-tooltip.top="slotProps.data[column.field]" class="kn-cursor-pointer"> {{ slotProps.data[column.field] }}</span>
                        </template>
                    </Column>
                    <Column :row-editor="true" :style="hierarchyManagementDimensionsTableDescriptor.iconColumnStyle"></Column>
                    <Column :style="hierarchyManagementDimensionsTableDescriptor.iconColumnStyle">
                        <template #body="slotProps">
                            <Button v-tooltip.top="$t('common.restore')" icon="pi pi-history" class="p-button-link" @click="restoreBackupConfirm(slotProps.data)" />
                            <Button v-tooltip.top="$t('common.delete')" icon="pi pi-trash" class="p-button-link" @click="deleteBackupConfirm(slotProps.data)" />
                        </template>
                    </Column>
                </DataTable>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iHierarchy, iNodeMetadata } from '../HierarchyManagement'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import hierarchyManagementHierarchiesCardDescriptor from '@/modules/managers/hierarchyManagement/HierarchyManagementMasterTab/HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesCardDescriptor.json'
import hierarchyManagementDimensionsTableDescriptor from '@/modules/managers/hierarchyManagement/HierarchyManagementMasterTab/HierarchyManagementDimensionsCard//HierarchyManagementDimensionsTable/HierarchyManagementDimensionsTableDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'hierarchy-management-technical-tab',
    components: { Dropdown, Column, DataTable },
    props: { dimensions: { type: Array as PropType<iDimension[]> } },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            hierarchyManagementHierarchiesCardDescriptor,
            hierarchyManagementDimensionsTableDescriptor,
            selectedDimension: null as iDimension | null,
            hierarchyType: '' as string,
            selectedHierarchy: null as iHierarchy | null,
            nodeMetadata: null as iNodeMetadata | null,
            hierarchies: [] as iHierarchy[],
            backupData: [] as any,
            rows: [] as any[],
            editingRows: [] as any[],
            columns: [] as { field: string; header: string }[],
            globalFilterFields: [] as string[],
            filters: { global: [filterDefault] },
            loading: false
        }
    },
    created() {},
    methods: {
        async onSelectedDimensionChange() {
            await this.loadNodeMetadata()
        },
        async onHierarchyTypeSelected() {
            this.selectedHierarchy = null
            await this.loadHierarchies()
        },
        async onHierarchySelected() {
            await this.loadBackupData()
        },
        async loadNodeMetadata() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `hierarchies/nodeMetadata?dimension=${this.selectedDimension?.DIMENSION_NM}&excludeLeaf=false`).then((response: AxiosResponse<any>) => (this.nodeMetadata = response.data))
        },
        async loadHierarchies() {
            const url = this.hierarchyType === 'MASTER' ? `hierarchiesMaster/getHierarchiesMaster?dimension=${this.selectedDimension?.DIMENSION_NM}` : `hierarchiesTechnical/getHierarchiesTechnical?dimension=${this.selectedDimension?.DIMENSION_NM}`
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => (this.hierarchies = response.data))
        },
        async loadBackupData() {
            this.loading = true
            const url = `hierarchiesBackup/getHierarchyBkps?dimension=${this.selectedDimension?.DIMENSION_NM}&hierarchyCode=${this.selectedHierarchy?.HIER_CD}&hierarchyName=${this.selectedHierarchy?.HIER_NM}&hierarchyType=${this.selectedHierarchy?.HIER_TP}`
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => {
                this.rows = response.data.root
                this.columns = response.data.columns
                    ?.filter((column: any) => column.VISIBLE)
                    .map((column: any) => {
                        return { field: column.ID, header: column.NAME }
                    })
                this.globalFilterFields = response.data.columns_search
            })
            this.loading = false
        },
        deleteBackupConfirm(hierarchy) {
            this.$confirm.require({
                header: this.$t('common.delete'),
                message: this.$t('managers.hierarchyManagement.deleteBackupConfirm'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteBackup(hierarchy)
            })
        },
        async deleteBackup(hierarchy) {
            const url = `hierarchies/deleteHierarchy`
            const postData = { dimension: this.selectedDimension?.DIMENSION_NM, name: hierarchy.HIER_NM }
            await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData).then((response: AxiosResponse<any>) => {
                if (response.data.response === 'ok') {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('managers.hierarchyManagement.backupDeleted')
                    })
                    this.loadBackupData()
                }
            })
        },
        updateBackupConfirm(event) {
            this.$confirm.require({
                header: this.$t('common.update'),
                message: this.$t('managers.hierarchyManagement.saveBackupMessage'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.updateBackup(event)
            })
        },
        async updateBackup(eventData) {
            const url = `hierarchiesBackup/modifyHierarchyBkps`
            const postData = { HIER_DS: eventData.newData.HIER_DS, HIER_NM: eventData.newData.HIER_NM, HIER_NM_ORIG: eventData.data.HIER_NM, dimension: this.selectedDimension?.DIMENSION_NM }
            await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData).then((response: AxiosResponse<any>) => {
                if (response.data.response === 'ok') {
                    this.store.setInfo({
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.loadBackupData()
                }
            })
        },
        restoreBackupConfirm(backup) {
            this.$confirm.require({
                header: this.$t('managers.hierarchyManagement.restoreTitle'),
                message: this.$t('managers.hierarchyManagement.restoreMsg'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.restoreBackup(backup)
            })
        },
        async restoreBackup(backup) {
            const url = `hierarchiesBackup/restoreHierarchyBkps`
            const postData = { code: backup.HIER_CD, name: backup.HIER_NM, dimension: this.selectedDimension?.DIMENSION_NM }
            await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData).then((response: AxiosResponse<any>) => {
                if (response.data.response === 'ok') {
                    this.store.setInfo({
                        title: this.$t('common.restore'),
                        msg: this.$t('common.toast.success')
                    })
                    this.loadBackupData()
                }
            })
        }
    }
})
</script>
