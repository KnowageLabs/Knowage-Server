<template>
    <KnPivotTable :id="id" :columns="filteredColumns" :rows="tempRows" :propConfiguration="propConfiguration" :entity="entity" @rowChanged="onRowChanged" @rowDeleted="onRowDeleted"></KnPivotTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnPivotTable from '@/components/UI/KnPivotTable.vue'
import mockColumns from './mockColumns.json'
import mockRows from './mockRows.json'

export default defineComponent({
    name: 'registry-pivot-datatable',
    components: { KnPivotTable },
    props: {
        columns: [] as any,
        rows: [] as any,
        propConfiguration: { type: Object },
        entity: { type: String },
        id: { type: String }
    },
    emits: ['rowChanged', 'rowDeleted'],
    data() {
        return {
            filteredColumns: [] as any[],
            tempRows: [] as any[]
        }
    },
    watch: {
        columns() {
            this.getFilteredColumns()
        },
        rows: {
            handler() {
                this.loadRows()
            },
            deep: true
        }
    },
    created() {
        this.getFilteredColumns()
        this.loadRows()
    },
    methods: {
        getFilteredColumns() {
            console.log('ZA MOCK: ', this.columns)
            this.filteredColumns = this.columns

            // MOCK
            this.filteredColumns = mockColumns
        },
        loadRows() {
            this.tempRows = this.rows

            // MOCK
            this.tempRows = mockRows
        },
        onRowChanged(row: any) {
            console.log('Changed Row: ', row)
            this.$emit('rowChanged', row)
        },
        onRowDeleted(row: any) {
            console.log('ROW FOR DELETE: ', row)
            this.$emit('rowDeleted', row)
        }
    }
})
</script>
