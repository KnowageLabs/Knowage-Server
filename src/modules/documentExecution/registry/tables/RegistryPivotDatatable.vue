<template>
    <KnPivotTable
        :id="id"
        :columns="filteredColumns"
        :rows="tempRows"
        :prop-configuration="propConfiguration"
        :entity="entity"
        :pagination="pagination"
        :combo-column-options="comboColumnOptions"
        :number-of-rows="registryDescriptor.paginationNumberOfItems"
        @rowChanged="onRowChanged"
        @pageChanged="onPageChange"
        @dropdownOpened="addColumnOptions"
    ></KnPivotTable>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import registryDescriptor from '../RegistryDescriptor.json'
import KnPivotTable from '@/components/UI/KnPivotTable/KnPivotTable.vue'

export default defineComponent({
    name: 'registry-pivot-datatable',
    components: { KnPivotTable },
    props: {
        columns: [] as any,
        rows: [] as any,
        propConfiguration: { type: Object },
        entity: { type: Object as PropType<string | null> },
        id: { type: String },
        propPagination: { type: Object }
    },
    emits: ['rowChanged', 'pageChanged', 'resetRows'],
    data() {
        return {
            registryDescriptor,
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
        },
        loadRows() {
            this.tempRows = this.rows

            if (this.tempRows.length <= registryDescriptor.paginationLimit) {
                this.lazy = false
                this.tempRows = this.tempRows.slice(0, registryDescriptor.paginationNumberOfItems)
            }
        },
        onRowChanged(row: any) {
            this.$emit('rowChanged', row)
        },
        loadPagination() {
            this.pagination = this.propPagination
        },
        onPageChange(event: any) {
            if (this.lazy) {
                this.$emit('pageChanged', event)
            } else {
                this.tempRows = this.rows.slice(event.paginationStart, event.paginationStart + registryDescriptor.paginationNumberOfItems)
                this.$emit('resetRows')
            }
        },
        addColumnOptions(payload: any) {
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
            await this.$http
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then((response: AxiosResponse<any>) => (this.comboColumnOptions[column.field][row[column.dependences]?.data] = response.data.rows))
        }
    }
})
</script>
