<template>
    <div class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow" :style="descriptor.style.preview">
        <!-- <div style="overflow: auto; height: 500px; width: 400px">
            {{ propWidget }}
        </div> -->

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
            emitter.on('sortingChanged', (sorting) => this.onColumnSort(sorting)) // { sortingColumn: this.widgetModel.settings.sortingColumn, sortingOrder: this.widgetModel.settings.sortingOrder }
            emitter.on('collumnAdded', () => this.onColumnAdd())
            emitter.on('collumnRemoved', () => this.onColumnDelete())
            emitter.on('collumnUpdated', (column) => this.onColumnUpdate(column))
            emitter.on('columnsReordered', () => this.onColumnReorder())
            emitter.on('indexColumnChanged', (rows) => this.onIndexColumnChange(rows))
            emitter.on('rowSpanChanged', (rows) => console.log('WidgetEditorPreview  - rowSpanChanged!', rows))
            emitter.on('summaryRowsChanged', () => console.log('WidgetEditorPreview  - summaryRowsChanged!'))
            emitter.on('headersConfigurationChanged', (headersConfiguration) => this.onHeaderChange(headersConfiguration))
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

            this.gridApi.setColumnDefs(this.getDatatableColumns())
            this.setInitialSorting()
            this.setInitialHeaders()
            this.setInitialIndexColumn()

            const updateData = (data) => {
                this.rowData = data
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
        setInitialIndexColumn() {
            if (this.propWidget.settings.configuration.rows && this.propWidget.settings.configuration.rows.indexColumn) {
                this.onIndexColumnChange(this.propWidget.settings.configuration.rows)
            }
        },
        getDatatableColumns() {
            console.log('COLUMNS IN MODEL -------------------', this.propWidget.columns)
            const columns = this.propWidget.columns.map((column, index) => {
                return { colId: column.id, field: `column_${index + 1}`, headerName: column.alias }
            })
            return columns
        },
        onColumnAdd() {
            console.log('ADDED -------------------')
            this.gridApi.setColumnDefs(this.getDatatableColumns())
        },
        onColumnDelete() {
            console.log('DELETED -------------------')
            this.gridApi.setColumnDefs(this.getDatatableColumns())
        },
        onColumnUpdate(column) {
            console.log('WidgetEditorPreview  - columnEdited!', column)
            this.gridApi.setColumnDefs(this.getDatatableColumns())
        },
        onColumnSort(sorting) {
            this.sortColumn(sorting)
        },
        onColumnReorder() {
            console.log('WidgetEditorPreview  - columnsReordered!')
            this.gridApi.setColumnDefs(this.getDatatableColumns())
        },
        onHeaderChange(headersConfiguration) {
            console.log('WidgetEditorPreview  - headersConfigurationChanged!', headersConfiguration)
            headersConfiguration.enabled ? this.gridApi.setHeaderHeight(25) : this.gridApi.setHeaderHeight(0)
        },
        onIndexColumnChange(rows) {
            console.log('INDEX COLUMN CHANGE -0000000000000000000000000')
            const columns = this.getDatatableColumns()
            rows.indexColumn ? columns.unshift({ colId: 'indexColumn', valueGetter: `node.rowIndex + 1`, headerName: '#' }) : ''
            this.gridApi.setColumnDefs(columns)
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
