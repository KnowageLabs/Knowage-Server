<template>
    <KnPivotTable :id="id" :columns="filteredColumns" :rows="tempRows" :propConfiguration="propConfiguration" :entity="entity" :pagination="pagination" :comboColumnOptions="comboColumnOptions" @rowChanged="onRowChanged" @pageChanged="onPageChange" @dropdownOpened="addColumnOptions"></KnPivotTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import KnPivotTable from '@/components/UI/KnPivotTable/KnPivotTable.vue'
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
        id: { type: String },
        propPagination: { type: Object }
    },
    emits: ['rowChanged', 'pageChanged', 'resetRows'],
    data() {
        return {
            filteredColumns: [] as any[],
            tempRows: [] as any[],
            pagination: {} as any,
            comboColumnOptions: [] as any[],
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

            // MOCK
            this.filteredColumns = mockColumns
        },
        loadRows() {
            this.tempRows = this.rows

            if (this.tempRows.length <= 1000) {
                this.lazy = false
                this.tempRows = this.tempRows.slice(0, 15)
            }

            // MOCK
            this.tempRows = mockRows
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
        },
        addColumnOptions(payload: any) {
            console.log('PAYLOAD: ', payload)
            const column = payload.column
            const row = payload.row
            if (!this.comboColumnOptions[column.field]) {
                this.comboColumnOptions[column.field] = []
            }

            if (!this.comboColumnOptions[column.field][row[column.dependences]?.data]) {
                this.loadColumnOptions(column, row)
            }
        },
        async loadColumnOptions(column: any, row: any) {
            const subEntity = column.subEntity ? '::' + column.subEntity + '(' + column.foreignKey + ')' : ''

            const entityId = this.entity + subEntity + ':' + column.field
            const entityOrder = this.entity + subEntity + ':' + (column.orderBy ?? column.field)

            const postData = new URLSearchParams({ ENTITY_ID: entityId, QUERY_TYPE: 'standard', ORDER_ENTITY: entityOrder, ORDER_TYPE: 'asc', QUERY_ROOT_ENTITY: 'true' })
            if (column.dependences && row && row[column.dependences].data) {
                postData.append('DEPENDENCES', this.entity + subEntity + ':' + column.dependences + '=' + row[column.dependences].data)
            }
            await axios
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then((response) => (this.comboColumnOptions[column.field][row[column.dependences]?.data] = response.data.rows))
            console.log('COLUMN OPTIONS AXIOS: ', this.comboColumnOptions)
        }
    }
})
</script>
