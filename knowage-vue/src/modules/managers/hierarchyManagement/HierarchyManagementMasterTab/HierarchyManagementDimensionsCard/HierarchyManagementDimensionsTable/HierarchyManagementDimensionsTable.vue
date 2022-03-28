<template>
    <div>
        <DataTable :value="rows" class="p-datatable-sm kn-table" v-model:filters="filters" :globalFilterFields="globalFilterFields" responsiveLayout="scroll" breakpoint="600px" :scrollable="true">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #header>
                <div class="table-header p-d-flex">
                    <span class="p-input-icon-left p-mr-3 p-col-12">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>
            <Column class="kn-truncated" v-for="column in columns" :header="$t(column.header)" :key="column.field" :sortField="column.field" :sortable="true">
                <template #body="slotProps">
                    <span v-tooltip.top="slotProps.data[column.field]" class="kn-cursor-pointer"> {{ slotProps.data[column.field] }}</span>
                </template>
            </Column>
            <Column :style="hierarchyManagementDimensionsTableDescriptor.iconColumnStyle">
                <template #body="slotProps">
                    <Button icon="pi pi-info" class="p-button-link" v-tooltip.top="$t('common.detail')" @click.stop="showInfo(slotProps.data)" />
                </template>
            </Column>
        </DataTable>

        <HierarchyManagementDimensionsInfoDialog :visible="infoDialogVisible" :selectedItem="selectedItem" @close="closeInfoDialog"></HierarchyManagementDimensionsInfoDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import hierarchyManagementDimensionsTableDescriptor from './HierarchyManagementDimensionsTableDescriptor.json'
import HierarchyManagementDimensionsInfoDialog from './HierarchyManagementDimensionsInfoDialog.vue'

export default defineComponent({
    name: 'hierarchy-management-dimensions-table',
    components: { Column, DataTable, HierarchyManagementDimensionsInfoDialog },
    props: { dimensionData: { type: Object } },
    data() {
        return {
            hierarchyManagementDimensionsTableDescriptor,
            rows: [] as any[],
            columns: [] as { field: string; header: string }[],
            filters: { global: [filterDefault] },
            globalFilterFields: [] as string[],
            selectedItem: [] as { value: string; label: string }[],
            infoDialogVisible: false
        }
    },
    watch: {
        dimensionData() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            console.log('LOADED DIMENSION DATA TABLE: ', this.dimensionData)
            if (!this.dimensionData) return

            this.rows = this.dimensionData.root
            this.columns = this.dimensionData.columns
                ?.filter((column: any) => column.VISIBLE)
                .map((column: any) => {
                    return { field: column.ID, header: column.NAME }
                })
            this.globalFilterFields = this.dimensionData.columns_search

            console.log('LOADED ROWS: ', this.rows)
            console.log('LOADED COLUMNS: ', this.columns)
        },
        showInfo(item: any) {
            console.log('SHOW INFO CLICKED FOR: ', item)
            this.selectedItem = []
            Object.keys(item).forEach((key: string) => this.selectedItem.push({ label: this.getColumnLabel(key), value: item[key] }))
            this.infoDialogVisible = true
        },
        getColumnLabel(key: string) {
            if (!this.dimensionData) return
            const index = this.dimensionData.columns.findIndex((column: any) => column.ID === key)
            return index !== -1 ? this.dimensionData.columns[index].NAME : ''
        },
        closeInfoDialog() {
            this.selectedItem = []
            this.infoDialogVisible = false
        }
    }
})
</script>
