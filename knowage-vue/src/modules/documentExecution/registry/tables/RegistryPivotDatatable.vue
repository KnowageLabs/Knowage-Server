<template>
    <KnPivotTable :id="id" :columns="filteredColumns" :rows="tempRows" :propConfiguration="propConfiguration" :entity="entity" :pagination="pagination" @rowChanged="onRowChanged" @pageChanged="onPageChange"></KnPivotTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnPivotTable from '@/components/UI/KnPivotTable.vue'

export default defineComponent({
    name: 'registry-pivot-datatable',
    components: { KnPivotTable },
    props: {
        columns: [] as any,
        rows: [] as any,
        propConfiguration: { type: Object },
        entity: { type: String },
        id: { type: String },
        propPagination: { type: Object }
    },
    emits: ['rowChanged', 'pageChanged', 'resetRows'],
    data() {
        return {
            filteredColumns: [] as any[],
            tempRows: [] as any[],
            pagination: {} as any,
            lazy: false
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
        },
        propPagination: {
            handler() {
                this.loadPagination()
            },
            deep: true
        }
    },
    created() {
        this.getFilteredColumns()
        this.loadRows()
        this.loadPagination()
    },
    methods: {
        getFilteredColumns() {
            this.filteredColumns = this.columns
        },
        loadRows() {
            this.tempRows = this.rows

            if (this.tempRows.length <= 1000) {
                this.lazy = false
                this.tempRows = this.tempRows.slice(0, 15)
            }
        },
        onRowChanged(row: any) {
            this.$emit('rowChanged', row)
        },
        loadPagination() {
            this.pagination = this.propPagination
            // console.log('LOADED PAGINATION WRAPPER: ', this.pagination)
        },
        onPageChange(event: any) {
            // console.log('ON PAGE CHANGE: ', event)
            // console.log('LAZY: ', this.lazy)
            if (this.lazy) {
                this.$emit('pageChanged', event)
            } else {
                this.tempRows = this.rows.slice(event.paginationStart, event.paginationStart + 15)
                this.$emit('resetRows')
            }

            // console.log('TEMP ROWS AFTER CHANGE: ', this.tempRows)
        }
    }
})
</script>
