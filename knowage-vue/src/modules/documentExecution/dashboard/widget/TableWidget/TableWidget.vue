<template>
    <ag-grid-vue class="kn-table-widget-grid ag-theme-alpine p-m-2" :style="getWidgetStyleString()" :gridOptions="gridOptions" :rowData="rowData" :columnDefs="columnDefs" @grid-ready="onGridReady"></ag-grid-vue>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent } from 'vue'
import { emitter } from '../../DashboardHelpers'
import mock from '../../dataset/DatasetEditorTestMocks.json'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import 'ag-grid-community/styles/ag-grid.css' // Core grid CSS, always needed
import 'ag-grid-community/styles/ag-theme-alpine.css' // Optional theme CSS
import HeaderRenderer from './TableWidgetHeaderRenderer.vue'

export default defineComponent({
    name: 'table-widget',
    components: { AgGridVue, HeaderRenderer },
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
            datasetRecordsRows: mock.mockResponse.rows as any,
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
            console.log('setEventListener')
            emitter.on('paginationChanged', (pagination) => console.log('WidgetEditorPreview - PAGINATION CHANGED!', pagination)) //  { enabled: this.paginationEnabled, itemsNumber: +this.itemsNumber }
            emitter.on('sortingChanged', (sorting) => this.sortColumn(sorting)) // { sortingColumn: this.widgetModel.settings.sortingColumn, sortingOrder: this.widgetModel.settings.sortingOrder }
            emitter.on('columnAdded', () => this.createDatatableColumns())
            emitter.on('columnRemoved', () => this.createDatatableColumns())
            emitter.on('collumnUpdated', () => this.createDatatableColumns())
            emitter.on('columnsReordered', () => this.createDatatableColumns())
            emitter.on('indexColumnChanged', (rows) => this.createDatatableColumns())
            emitter.on('rowSpanChanged', (rows) => this.createDatatableColumns())
            emitter.on('summaryRowsChanged', (rows) => this.createDatatableColumns()) //TODO: Servis nam treba za ovo
            emitter.on('headersConfigurationChanged', () => this.createDatatableColumns()) // TODO: Trenutno se gleda svaka promena u header config, mozda staviti event emit samo na promene koje trebaju.
            emitter.on('columnGroupsConfigurationChanged', () => this.createDatatableColumns())
            emitter.on('exportModelChanged', (exportModel) => console.log('WidgetEditorPreview  - exportModelChanged!', exportModel))
            emitter.on('visualizationTypeChanged', (visuelizationTypes) => console.log('WidgetEditorPreview  - visualizationTypeChanged!', visuelizationTypes))
            emitter.on('visibilityConditionsChanged', (visibilityConditions) => console.log('WidgetEditorPreview  - visibilityConditionsChanged!', visibilityConditions))
            emitter.on('headersStyleChanged', (headersStyle) => this.changeHeaderHeight(headersStyle))
            emitter.on('columnStylesChanged', (columnStyles) => console.log('WidgetEditorPreview  - columnStylesChanged!', columnStyles))
            emitter.on('columnGroupStylesChanged', (columnGroupStyles) => console.log('WidgetEditorPreview  - columnGroupStylesChanged!', columnGroupStyles))
            emitter.on('rowsStyleChanged', (rowsStyle) => console.log('WidgetEditorPreview  - rowsStyleChanged!', rowsStyle))
            emitter.on('summaryStyleChanged', (summaryStyle) => console.log('WidgetEditorPreview  - summaryStyleChanged!', summaryStyle))
            emitter.on('bordersStyleChanged', (bordersStyle) => console.log('WidgetEditorPreview  - bordersStyleChanged!', bordersStyle))
            emitter.on('paddingStyleChanged', (paddingStyle) => console.log('WidgetEditorPreview  - paddingStyleChanged!', paddingStyle))
            emitter.on('shadowStyleChanged', (shadowsStyle) => console.log('WidgetEditorPreview  - shadowStyleChanged!', shadowsStyle))
            emitter.on('conditionalStylesChanged', (conditionalStyles) => console.log('WidgetEditorPreview  - conditionalStylesChanged!', conditionalStyles))
            emitter.on('tooltipsChanged', (tooltips) => console.log('WidgetEditorPreview  - tooltipsChanged!', tooltips))
        },
        setupDatatableOptions() {
            this.gridOptions = {
                // PROPERTIES
                defaultColDef: this.defaultColDef,
                pagination: false,
                rowSelection: 'single',
                suppressRowTransform: true,
                rowHeight: 25,
                components: {
                    agColumnHeader: HeaderRenderer
                },

                // EVENTS
                onRowClicked: (event, params) => console.log('A row was clicked', event),
                onCellClicked: (event, params) => console.log('A cell was clicked', event),
                onColumnResized: (event) => console.log('A column was resized'),
                onGridReady: (event) => console.log('The grid is now ready')

                // CALLBACKS
                // getRowHeight: (params) => 25
            }
        },
        onGridReady(params) {
            this.gridApi = params.api
            this.columnApi = params.columnApi

            this.createDatatableColumns()
        },
        createDatatableColumns() {
            const datatableColumns = this.getTableColumns(this.mock.mockResponse.metaData.fields)
            this.toggleHeaders(this.propWidget.settings.configuration.headers)
            this.gridApi.setColumnDefs(datatableColumns)

            const updateData = (data) => {
                this.rowData = data.slice(0, this.propWidget.settings.pagination.itemsNumber)

                if (this.propWidget.settings.configuration.summaryRows.enabled) {
                    this.gridApi.setPinnedBottomRowData(data.slice(this.propWidget.settings.pagination.itemsNumber))
                } else this.gridApi.setPinnedBottomRowData()
            }
            updateData(this.datasetRecordsRows)
        },
        sortColumn(sorting) {
            this.columnApi.applyColumnState({
                state: [{ colId: sorting.sortingColumn, sort: sorting.sortingOrder.toLowerCase() }],
                defaultState: { sort: null }
            })
        },
        toggleHeaders(headersConfiguration) {
            headersConfiguration.enabled ? this.gridApi.setHeaderHeight(this.propWidget.settings.style.headers.height) : this.gridApi.setHeaderHeight(0)
        },
        getTableColumns(responseFields) {
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

                    if (typeof responseFields[responseField] == 'object' && ((dataset.type == 'SbiSolrDataSet' && thisColumn.columnName.toLowerCase() === responseFields[responseField].header) || thisColumn.columnName.toLowerCase() === responseFields[responseField].header.toLowerCase())) {
                        this.columnsNameArray.push(responseFields[responseField].name)
                        var tempCol = {
                            colId: this.propWidget.columns[datasetColumn].id,
                            headerName: this.propWidget.columns[datasetColumn].alias,
                            field: responseFields[responseField].name,
                            measure: this.propWidget.columns[datasetColumn].fieldType,
                            headerComponentParams: { styleString: this.getWidgetStyleByType('headers', true) }
                        } as any

                        if (this.propWidget.columns[datasetColumn].style && this.propWidget.columns[datasetColumn].style.enableCustomHeaderTooltip) {
                            // tempCol.headerTooltip = replacePlaceholders(this.propWidget.columns[datasetColumn].style.customHeaderTooltip, null, true)
                            //TODO: add custom tooltips for headers if there are any
                        } else {
                            tempCol.headerTooltip = this.propWidget.columns[datasetColumn].alias
                        }

                        if (tempCol.measure === 'MEASURE') tempCol.aggregationSelected = this.propWidget.columns[datasetColumn].aggregation
                        // tempCol.pinned = this.propWidget.columns[datasetColumn].pinned

                        if (this.propWidget.columns[datasetColumn].isCalculated) {
                            tempCol.isCalculated = this.propWidget.columns[datasetColumn].isCalculated
                        }

                        //ROWSPAN MANAGEMENT
                        if (this.propWidget.settings.configuration.rows.rowSpan.column === this.propWidget.columns[datasetColumn].id) {
                            var previousValue
                            var previousIndex
                            var tempRows = this.datasetRecordsRows as any
                            for (var r in tempRows as any) {
                                if (previousValue != tempRows[r][responseFields[responseField].name]) {
                                    previousValue = tempRows[r][responseFields[responseField].name]
                                    previousIndex = r
                                    tempRows[r].span = 1
                                } else {
                                    tempRows[previousIndex].span++
                                }
                            }
                            tempCol.rowSpan = function RowSpanCalculator(params) {
                                if (params.data.span > 1) {
                                    return params.data.span
                                } else return 1
                            }
                            tempCol.cellClassRules = {
                                'cell-span': function (params) {
                                    return tempRows[params.rowIndex].span > 1
                                }
                            }
                        }
                        // SUMMARY ROW  -----------------------------------------------------------------
                        if (this.propWidget.settings.configuration.summaryRows.enabled) {
                            tempCol.cellRendererSelector = (params) => {
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
                        }

                        // HEADERS CONFIGURATION  -----------------------------------------------------------------
                        var headersConfiguration = this.propWidget.settings.configuration.headers
                        if (headersConfiguration.enabled && headersConfiguration.custom.enabled) {
                            headersConfiguration.custom.rules.forEach((rule) => {
                                rule.target.forEach((columnId) => {
                                    if (columnId === tempCol.colId) {
                                        switch (rule.action) {
                                            case 'hide':
                                                tempCol.headerName = ''
                                                break
                                            case 'setLabel':
                                                rule.value ? (tempCol.headerName = rule.value) : ''
                                                break
                                        }
                                    }
                                })
                            })
                        }
                        headersConfiguration.test = 'TEEEEEEEEEEEEEEEEEEEEEEEST'

                        // COLUMN GROUPING -----------------------------------------------------------------
                        var group = this.getColumnGroup(this.propWidget.columns[datasetColumn])
                        if (group) {
                            if (typeof columnGroups[group.id] != 'undefined') {
                                columns[columnGroups[group.id]].children.push(tempCol)
                            } else {
                                columnGroups[group.id] = columns.length
                                columns.push({
                                    headerName: group.label,
                                    children: [tempCol]
                                })
                            }
                        } else columns.push(tempCol)
                        break
                    }
                }
            }

            return columns
        },
        getColumnGroup(col) {
            var modelGroups = this.propWidget.settings.configuration.columnGroups.groups
            if (this.propWidget.settings.configuration.columnGroups.enabled && modelGroups && modelGroups.length > 0) {
                for (var k in modelGroups) {
                    if (modelGroups[k].columns.includes(col.id)) {
                        return modelGroups[k]
                    }
                }
            } else return false
        },
        getWidgetStyleByType(styleType: string, overrideEnable?: boolean) {
            const styleSettings = this.propWidget.settings.style[styleType]
            if (styleSettings.enabled || overrideEnable) {
                const styleString = Object.entries(styleSettings.properties)
                    .map(([k, v]) => `${k}:${v}`)
                    .join(';')
                return styleString + ';'
            } else return ''
        },
        getWidgetStyleString() {
            const styleString = this.getWidgetStyleByType('shadows') + this.getWidgetStyleByType('padding') + this.getWidgetStyleByType('borders')
            return styleString
        },
        changeHeaderHeight(headersStyle) {
            console.log('change hewight - ', headersStyle)
            this.propWidget.settings.configuration.headers.enabled ? this.gridApi.setHeaderHeight(headersStyle.height) : ''
        }
    }
})
</script>
<style lang="scss">
.cell-span {
    background: white;
    border-left: 1px solid lightgrey !important;
    border-right: 1px solid lightgrey !important;
    border-bottom: 1px solid lightgrey !important;
}
</style>
