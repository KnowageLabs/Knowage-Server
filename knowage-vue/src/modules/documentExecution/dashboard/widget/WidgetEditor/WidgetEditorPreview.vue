<template>
    <div class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow" :style="descriptor.style.preview">
        <div style="overflow: auto; height: 500px; width: 400px">
            {{ propWidget }}
        </div>

        <ag-grid-vue style="width: 100%; height: 100%" class="ag-theme-alpine" :columnDefs="columnDefs" :rowData="rowData" :defaultColDef="defaultColDef"></ag-grid-vue>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent, reactive } from 'vue'
import { IWidgetColumn } from '../../Dashboard'
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
            rowData: [] as any,
            columnDefs: reactive({}),
            defaultColDef: {
                sortable: true,
                filter: true,
                flex: 1
            }
        }
    },
    created() {
        this.setEventListeners()
        this.columnDefs = this.propWidget.columns.map((column, index) => {
            return { colId: column.id, field: `column_${index + 1}`, headerName: column.alias }
        })
    },
    mounted() {
        this.defineDatatableColumns()
    },

    methods: {
        defineDatatableColumns() {
            console.log('COLUMNS IN MODEL -------------------', this.propWidget.columns)
            this.columnDefs = this.propWidget.columns.map((column, index) => {
                return { colId: column.id, field: `column_${index + 1}`, headerName: column.alias }
            })
            setTimeout(() => {
                console.log('COLUMN DEFS -----------------------', this.columnDefs)
            }, 300)
        },
        setEventListeners() {
            emitter.on('paginationChanged', (pagination) => console.log('WidgetEditorPreview - PAGINATION CHANGED!', pagination)) //  { enabled: this.paginationEnabled, itemsNumber: +this.itemsNumber }
            emitter.on('sortingChanged', (sorting) => console.log('WidgetEditorPreview  - SORTING CHANGED!', sorting)) // { sortingColumn: this.widgetModel.settings.sortingColumn, sortingOrder: this.widgetModel.settings.sortingOrder }
            emitter.on('collumnAdded', (column) => this.onColumnAdd(column))
            emitter.on('collumnRemoved', (column) => console.log('WidgetEditorPreview  - collumnRemoved!', column, this.propWidget))
            emitter.on('collumnUpdated', (column) => this.onColumnUpdate(column))
            emitter.on('columnsReordered', () => console.log('WidgetEditorPreview  - columnsReordered!'))
            emitter.on('indexColumnChanged', (rows) => console.log('WidgetEditorPreview  - indexColumnChanged!', rows))
            emitter.on('rowSpanChanged', (rows) => console.log('WidgetEditorPreview  - rowSpanChanged!', rows))
            emitter.on('summaryRowsChanged', () => console.log('WidgetEditorPreview  - summaryRowsChanged!'))
            emitter.on('headersConfigurationChanged', (headersConfiguration) => console.log('WidgetEditorPreview  - headersConfigurationChanged!', headersConfiguration))
        },
        onColumnAdd(column) {
            console.log('WidgetEditorPreview  - collumnAdded!', column)
        },
        onColumnUpdate(column) {
            console.log('WidgetEditorPreview  - columnEdited!', column)
        }
    }
})
</script>
