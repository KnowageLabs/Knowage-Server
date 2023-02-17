<template>
    <div class="p-mt-2">
        <DataTable v-model:filters="filters" :value="rows" class="p-datatable-sm kn-table" :global-filter-fields="globalFilterFields">
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
            <Column :style="hierarchyManagementDimensionsTableDescriptor.smallIconColumn">
                <template #body="slotProps">
                    <div class="pi pi-bars p-button-link" :draggable="true" @dragstart.stop="onDragStart($event, slotProps.data)" />
                </template>
            </Column>
            <Column v-for="column in columns" :key="column.field" class="kn-truncated" :header="$t(column.header)" :sort-field="column.field" :sortable="true">
                <template #body="slotProps">
                    <span v-tooltip.top="slotProps.data[column.field]" class="kn-cursor-pointer" :draggable="true" @dragstart="onDragStart($event, slotProps.data)"> {{ slotProps.data[column.field] }}</span>
                </template>
            </Column>
            <Column :style="hierarchyManagementDimensionsTableDescriptor.smallIconColumn">
                <template #body="slotProps">
                    <Button v-tooltip.top="$t('common.detail')" icon="pi pi-info" class="p-button-link" @click.stop="showInfo(slotProps.data)" />
                </template>
            </Column>
        </DataTable>

        <HierarchyManagementDimensionsInfoDialog :visible="infoDialogVisible" :selected-item="selectedItem" @close="closeInfoDialog" />
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
            if (!this.dimensionData) return

            this.rows = this.dimensionData.root
            this.columns = this.dimensionData.columns
                ?.filter((column: any) => column.VISIBLE)
                .map((column: any) => {
                    return { field: column.ID, header: column.NAME }
                })
            this.globalFilterFields = this.dimensionData.columns_search
        },
        showInfo(item: any) {
            this.selectedItem = []
            Object.keys(item).forEach((key: string) => this.selectedItem.push({ label: this.getColumnLabel(key), value: item[key] }))
            this.infoDialogVisible = true
        },
        getColumnLabel(key: string) {
            if (!this.dimensionData) return
            const index = this.dimensionData.columns.findIndex((column: any) => column.ID === key)
            return index !== -1 ? this.dimensionData.columns[index].NAME : ''
        },
        onDragStart(event: any, item: any) {
            event.dataTransfer.setData('text/plain', JSON.stringify(item))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        closeInfoDialog() {
            this.selectedItem = []
            this.infoDialogVisible = false
        }
    }
})
</script>
