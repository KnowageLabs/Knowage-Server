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
            columnsNameArray: [] as any,
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
    mounted() {},

    methods: {
        setEventListeners() {
            emitter.on('paginationChanged', (pagination) => console.log('WidgetEditorPreview - PAGINATION CHANGED!', pagination)) //  { enabled: this.paginationEnabled, itemsNumber: +this.itemsNumber }
            emitter.on('sortingChanged', (sorting) => this.sortColumn(sorting)) // { sortingColumn: this.widgetModel.settings.sortingColumn, sortingOrder: this.widgetModel.settings.sortingOrder }
            emitter.on('columnAdded', () => this.createDatatableColumns())
            emitter.on('columnRemoved', () => this.createDatatableColumns())
            emitter.on('collumnUpdated', () => this.createDatatableColumns())
            emitter.on('columnsReordered', () => this.createDatatableColumns())
            emitter.on('indexColumnChanged', (rows) => this.createDatatableColumns())
            emitter.on('rowSpanChanged', (rows) => console.log('WidgetEditorPreview  - rowSpanChanged!', rows))
            emitter.on('summaryRowsChanged', (rows) => this.createDatatableColumns()) //TODO: Servis nam treba za ovo
            // TODO: Trenutno se gleda svaka promena u header config, mozda staviti event emit samo na promene koje trebaju.
            emitter.on('headersConfigurationChanged', () => this.createDatatableColumns())
            emitter.on('columnGroupsConfigurationChanged', (columnGroupConfiguration) => console.log('WidgetEditorPreview  - columnGroupsConfigurationChanged!', columnGroupConfiguration))
            emitter.on('exportModelChanged', (exportModel) => console.log('WidgetEditorPreview  - exportModelChanged!', exportModel))
            emitter.on('visualizationTypeChanged', (visuelizationTypes) => console.log('WidgetEditorPreview  - visualizationTypeChanged!', visuelizationTypes))
        },
        setupDatatableOptions() {
            this.gridOptions = {
                // PROPERTIES
                defaultColDef: this.defaultColDef,
                pagination: false,
                rowSelection: 'single',
                suppressRowTransform: true,

                // EVENTS
                onRowClicked: (event, params) => console.log('A row was clicked', event),
                onCellClicked: (event, params) => console.log('A cell was clicked', event),
                onColumnResized: (event) => console.log('A column was resized'),
                onGridReady: (event) => console.log('The grid is now ready'),

                // CALLBACKS
                getRowHeight: (params) => 25
            }
        },
        onGridReady(params) {
            this.gridApi = params.api
            this.columnApi = params.columnApi

            this.createDatatableColumns()
        },
        createDatatableColumns() {
            // const datatableColumns = this.getDatatableColumns()

            // this.setSorting()
            // this.setIndexColumn(this.propWidget.settings.configuration.rows.indexColumn, datatableColumns)
            // this.setHeaders(this.propWidget.settings.configuration.headers, datatableColumns)

            // datatableColumns[1].children = [{ field: 'athlete' }, { field: 'age' }, { field: 'country' }]

            // this.gridApi.setColumnDefs(datatableColumns)

            const updateData = (data) => {
                this.rowData = data.slice(0, this.propWidget.settings.pagination.itemsNumber)

                if (this.propWidget.settings.configuration.summaryRows.enabled) {
                    this.gridApi.setPinnedBottomRowData(data.slice(this.propWidget.settings.pagination.itemsNumber))
                } else this.gridApi.setPinnedBottomRowData()
            }
            updateData(this.mock.mockResponse.rows)

            this.gridApi.setColumnDefs(this.newGetColumns(this.mock.mockResponse.metaData.fields))
        },

        getDatatableColumns() {
            const columns = this.propWidget.columns.map((column, index) => {
                return {
                    colId: column.id,
                    field: `column_${index + 1}`,
                    headerName: column.alias,
                    cellRendererSelector: (params) => {
                        if (params.node.rowPinned && this.propWidget.settings.configuration.summaryRows.enabled) {
                            return {
                                component: SummaryRowRenderer,
                                params: {
                                    summaryRows: this.propWidget.settings.configuration.summaryRows.list.map((row) => {
                                        return row.label
                                    })
                                }
                            }
                        } else {
                            // rows that are not pinned don't use any cell renderer
                            return undefined
                        }
                    }
                }
            })
            class SummaryRowRenderer {
                eGui: HTMLDivElement | undefined
                init(params) {
                    this.eGui = document.createElement('div')
                    params.value ? (this.eGui.innerHTML = '<b style="margin-right: 4px;">' + params.summaryRows[params.rowIndex] + '</b>') : ''
                    this.eGui.innerHTML += params.value
                }
                getGui() {
                    return this.eGui
                }
                refresh() {
                    return false
                }
            }

            return columns
        },
        setSorting() {
            if (this.propWidget.settings.sortingColumn && this.propWidget.settings.sortingOrder) {
                this.sortColumn({ sortingColumn: this.propWidget.settings.sortingColumn, sortingOrder: this.propWidget.settings.sortingOrder })
            }
        },
        sortColumn(sorting) {
            this.columnApi.applyColumnState({
                state: [{ colId: sorting.sortingColumn, sort: sorting.sortingOrder.toLowerCase() }],
                defaultState: { sort: null }
            })
        },
        setIndexColumn(showIndexColumn, datatableColumns) {
            showIndexColumn ? datatableColumns.unshift({ colId: 'indexColumn', valueGetter: `node.rowIndex + 1`, headerName: '#', pinned: 'left', width: 50, sortable: false, filter: false }) : ''
        },
        setHeaders(headersConfiguration, datatableColumns) {
            headersConfiguration.enabled ? this.gridApi.setHeaderHeight(25) : this.gridApi.setHeaderHeight(0)

            if (headersConfiguration.enabled && headersConfiguration.custom.enabled) {
                headersConfiguration.custom.rules.forEach((rule) => {
                    rule.target.forEach((columnId) => {
                        var columnIndex = datatableColumns.findIndex((datatableColumn) => datatableColumn.colId == columnId)
                        switch (rule.action) {
                            case 'hide':
                                datatableColumns[columnIndex].headerName = ''
                                break
                            case 'setLabel':
                                rule.value ? (datatableColumns[columnIndex].headerName = rule.value) : ''
                                break
                        }
                    })
                })
            }
        },
        newGetColumns(responseFields) {
            var columns = [] as any
            var columnGroups = {}
            this.columnsNameArray = []

            // TODO: Get whole dataset here i guess...
            var dataset = { type: 'SbiFileDataSet' }

            if (this.propWidget.settings.configuration.rows.indexColumn) {
                columns.push({ colId: 'indexColumn', valueGetter: `node.rowIndex + 1`, headerName: '#', pinned: 'left', width: 50, sortable: false, filter: false })
            }
            // c = datasetColumn
            // f = responseField, fields = responseFields
            // this.propWidget.columns[datasetColumn] = this.propWidget.columns[datasetColumn]
            for (var datasetColumn in this.propWidget.columns) {
                for (var responseField in responseFields) {
                    var thisColumn = this.propWidget.columns[datasetColumn]

                    if (typeof responseFields[responseField] == 'object' && ((dataset.type == 'SbiSolrDataSet' && thisColumn.columnName.toLowerCase() === responseFields[responseField].header) || thisColumn.alias.toLowerCase() === responseFields[responseField].header.toLowerCase())) {
                        this.columnsNameArray.push(responseFields[responseField].name)
                        var tempCol = { headerName: this.propWidget.columns[datasetColumn].alias, field: responseFields[responseField].name, measure: this.propWidget.columns[datasetColumn].fieldType } as any

                        if (this.propWidget.columns[datasetColumn].style && this.propWidget.columns[datasetColumn].style.enableCustomHeaderTooltip) {
                            // tempCol.headerTooltip = replacePlaceholders(this.propWidget.columns[datasetColumn].style.customHeaderTooltip, null, true)
                            //TODO: add custom tooltips for headers if there are any
                        } else {
                            tempCol.headerTooltip = this.propWidget.columns[datasetColumn].alias
                        }

                        if (tempCol.measure === 'MEASURE') tempCol.aggregationSelected = this.propWidget.columns[datasetColumn].aggregation
                        // tempCol.pinned = this.propWidget.columns[datasetColumn].pinned

                        columns.push(tempCol)
                    }
                }
            }

            console.log(this.columnsNameArray)

            console.log('COLUMNS --------------------------')
            console.log(columns)
            return columns
        }
    }
})
</script>
