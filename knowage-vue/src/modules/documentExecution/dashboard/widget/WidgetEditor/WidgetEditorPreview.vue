<template>
    <div class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow" :style="descriptor.style.preview">
        <div style="overflow: auto; height: 500px; width: 400px">
            {{ propWidget }}
        </div>

        <ag-grid-vue style="flex: 0.5; min-width: 500px" class="ag-theme-alpine" :gridOptions="gridOptions" :rowData="rowData" :columnDefs="columnDefs" @grid-ready="onGridReady"></ag-grid-vue>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent, reactive } from 'vue'
import { emitter } from '../../DashboardHelpers'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import mock from '../../dataset/DatasetEditorTestMocks.json'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import 'ag-grid-community/styles/ag-grid.css' // Core grid CSS, always needed
import 'ag-grid-community/styles/ag-theme-alpine.css' // Optional theme CSS

export default defineComponent({
    name: 'widget-editor-preview',
    components: { Column, DataTable, AgGridVue },
    props: {
        propWidget: {
            required: true,
            type: Object
        }
    },
    data() {
        return {
            descriptor,
            mock,
            gridOptions: null as any,
            rowData: [] as any,
            columnDefs: [] as any,
            defaultColDef: {
                flex: 1
            },
            gridApi: null as any,
            columnApi: null as any
        }
    },
    created() {
        this.setEventListeners()
        this.setupDatatableOptions()
    },
    mounted() {
        this.getDatatableColumns()
    },

    methods: {
        setEventListeners() {
            emitter.on('paginationChanged', (pagination) => console.log('WidgetEditorPreview - PAGINATION CHANGED!', pagination)) //  { enabled: this.paginationEnabled, itemsNumber: +this.itemsNumber }
            emitter.on('sortingChanged', (sorting) => this.sortColumn(sorting)) // { sortingColumn: this.widgetModel.settings.sortingColumn, sortingOrder: this.widgetModel.settings.sortingOrder }
            emitter.on('collumnAdded', () => this.gridApi.setColumnDefs(this.getDatatableColumns()))
            emitter.on('collumnRemoved', () => this.gridApi.setColumnDefs(this.getDatatableColumns()))
            emitter.on('collumnUpdated', () => this.gridApi.setColumnDefs(this.getDatatableColumns()))
            emitter.on('columnsReordered', () => this.gridApi.setColumnDefs(this.getDatatableColumns()))
            emitter.on('indexColumnChanged', (rows) => this.gridApi.setColumnDefs(this.getDatatableColumns(rows.indexColumn)))
            emitter.on('rowSpanChanged', (rows) => console.log('WidgetEditorPreview  - rowSpanChanged!', rows))
            emitter.on('summaryRowsChanged', (rows) => console.log('WidgetEditorPreview  - summaryRowsChanged!', rows))
            emitter.on('headersConfigurationChanged', (headersConfiguration) => this.onHeaderChange(headersConfiguration))
            emitter.on('columnGroupsConfigurationChanged', (columnGroupConfiguration) => console.log('WidgetEditorPreview  - columnGroupsConfigurationChanged!', columnGroupConfiguration))
        },
        setupDatatableOptions() {
            this.gridOptions = {
                // PROPERTIES
                defaultColDef: this.defaultColDef,
                pagination: false,
                rowSelection: 'single',

                // EVENTS
                onRowClicked: (event) => console.log('A row was clicked'),
                onColumnResized: (event) => console.log('A column was resized'),
                onGridReady: (event) => console.log('The grid is now ready'),

                // CALLBACKS
                getRowHeight: (params) => 25
            }
        },
        onGridReady(params) {
            this.gridApi = params.api
            this.columnApi = params.columnApi

            this.gridApi.setColumnDefs(this.getDatatableColumns(this.propWidget.settings.configuration.rows.indexColumn))
            this.setInitialSorting()
            this.setInitialHeaders()

            const updateData = (data) => {
                this.rowData = data.slice(0, this.propWidget.settings.pagination.itemsNumber)
                console.log(data.slice(this.propWidget.settings.pagination.itemsNumber))
                this.gridApi.setPinnedBottomRowData(data.slice(this.propWidget.settings.pagination.itemsNumber))
            }

            updateData(this.mock.mockResponse.rows)
        },
        setInitialSorting() {
            if (this.propWidget.settings.sortingColumn && this.propWidget.settings.sortingOrder) {
                this.sortColumn({ sortingColumn: this.propWidget.settings.sortingColumn, sortingOrder: this.propWidget.settings.sortingOrder })
            }
        },
        setInitialHeaders() {
            if (this.propWidget.settings.configuration.headers && this.propWidget.settings.configuration.headers.enabled) {
                this.onHeaderChange(this.propWidget.settings.configuration.headers)
            }
        },
        getDatatableColumns(showIndexColumn?) {
            const columns = this.propWidget.columns.map((column, index) => {
                return { colId: column.id, field: `column_${index + 1}`, headerName: column.alias }
            })

            showIndexColumn ? columns.unshift({ colId: 'indexColumn', valueGetter: `node.rowIndex + 1`, headerName: '#' }) : ''

            return columns
        },
        onHeaderChange(headersConfiguration) {
            console.log('WidgetEditorPreview  - headersConfigurationChanged!', headersConfiguration)
            headersConfiguration.enabled ? this.gridApi.setHeaderHeight(25) : this.gridApi.setHeaderHeight(0)
        },
        sortColumn(sorting) {
            this.columnApi.applyColumnState({
                state: [{ colId: sorting.sortingColumn, sort: sorting.sortingOrder.toLowerCase() }],
                defaultState: { sort: null }
            })
        }
    }
})
</script>
