<template>
    <!-- <ag-grid-vue class="kn-table-widget-grid ag-theme-alpine p-m-2" :style="getWidgetStyleString()" :gridOptions="gridOptions" :rowData="rowData" :columnDefs="columnDefs" :tooltipShowDelay="100" :tooltipMouseTrack="true" @grid-ready="onGridReady"></ag-grid-vue> -->
    <ag-grid-vue class="kn-table-widget-grid ag-theme-alpine p-m-2" :gridOptions="gridOptions" :rowData="rowData" :columnDefs="columnDefs" :tooltipShowDelay="100" :tooltipMouseTrack="true" :overlayNoRowsTemplate="overlayNoRowsTemplateTest" @grid-ready="onGridReady"></ag-grid-vue>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent, PropType } from 'vue'
import { emitter } from '../../DashboardHelpers'
import mock from '../../dataset/DatasetEditorTestMocks.json'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import 'ag-grid-community/styles/ag-grid.css' // Core grid CSS, always needed
import 'ag-grid-community/styles/ag-theme-alpine.css' // Optional theme CSS
import HeaderRenderer from './HeaderRenderer.vue'
import SummaryRowRenderer from './SummaryRowRenderer.vue'
import HeaderGroupRenderer from './HeaderGroupRenderer.vue'
import TooltipRenderer from './TooltipRenderer.vue'
import CellRenderer from './CellRenderer.vue'
import { getWidgetStyleByType, getColumnConditionalStyles, isConditionMet } from './TableWidgetHelper'
import { IWidget } from '../../Dashboard'

export default defineComponent({
    name: 'table-widget',
    components: { AgGridVue, HeaderRenderer, SummaryRowRenderer, HeaderGroupRenderer, TooltipRenderer },
    props: {
        propWidget: {
            type: Object as PropType<IWidget>,
            required: true
        },
        editorMode: {
            type: Boolean,
            required: false
        }
    },
    watch: {
        propWidget: {
            handler() {
                if (!this.editorMode) this.createDatatableColumns()
            },
            deep: true
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
            columnApi: null as any,
            overlayNoRowsTemplateTest: null as any
        }
    },
    created() {
        if (this.editorMode) this.setEventListeners()
        this.setupDatatableOptions()
    },
    unmounted() {
        emitter.off('refreshTable', this.createDatatableColumns)
    },
    mounted() {},

    methods: {
        setEventListeners() {
            console.log('setEventListener')
            // emitter.on('paginationChanged', (pagination) => console.log('WidgetEditorPreview - PAGINATION CHANGED!', pagination)) //  { enabled: this.paginationEnabled, itemsNumber: +this.itemsNumber }
            emitter.on('sortingChanged', this.sortColumn)
            emitter.on('refreshTable', this.createDatatableColumns)
        },
        setupDatatableOptions() {
            this.gridOptions = {
                // PROPERTIES
                defaultColDef: this.defaultColDef,
                pagination: false,
                rowSelection: 'single',
                suppressRowTransform: true,
                suppressMovableColumns: true,
                suppressDragLeaveHidesColumns: true,
                suppressRowGroupHidesColumns: true,
                rowHeight: 25,

                // EVENTS
                // onCellClicked: (event, params) => console.log('A cell was clicked', event, params),

                // CALLBACKS
                getRowStyle: this.getRowStyle
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
                if (this.propWidget.settings.configuration.summaryRows.enabled) {
                    var rowsNumber = this.propWidget.settings.configuration.summaryRows.list.length
                    this.gridApi.setRowData(data.slice(0, data.length - rowsNumber))
                    this.gridApi.setPinnedBottomRowData(data.slice(-rowsNumber))
                } else {
                    this.gridApi.setRowData(data)
                    this.gridApi.setPinnedBottomRowData()
                }
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

            // TODO: Get whole dataset here when we get the BE service...
            var dataset = { type: 'SbiFileDataSet' }

            if (this.propWidget.settings.configuration.rows.indexColumn) {
                columns.push({
                    colId: 'indexColumn',
                    valueGetter: `node.rowIndex + 1`,
                    headerName: '',
                    pinned: 'left',
                    width: 55,
                    sortable: false,
                    filter: false,
                    headerComponent: HeaderRenderer,
                    headerComponentParams: { propWidget: this.propWidget },
                    cellRenderer: CellRenderer,
                    cellRendererParams: { colId: 'indexColumn', propWidget: this.propWidget }
                })
            }

            for (var datasetColumn in this.propWidget.columns) {
                for (var responseField in responseFields) {
                    var thisColumn = this.propWidget.columns[datasetColumn]

                    if (typeof responseFields[responseField] == 'object' && ((dataset.type == 'SbiSolrDataSet' && thisColumn.columnName.toLowerCase() === responseFields[responseField].header) || thisColumn.columnName.toLowerCase() === responseFields[responseField].header.toLowerCase())) {
                        this.columnsNameArray.push(responseFields[responseField].name)
                        var tempCol = {
                            hide: this.getColumnVisibilityCondition(this.propWidget.columns[datasetColumn].id),
                            colId: this.propWidget.columns[datasetColumn].id,
                            headerName: this.propWidget.columns[datasetColumn].alias,
                            field: responseFields[responseField].name,
                            measure: this.propWidget.columns[datasetColumn].fieldType,
                            headerComponent: HeaderRenderer,
                            headerComponentParams: { colId: this.propWidget.columns[datasetColumn].id, propWidget: this.propWidget },
                            cellRenderer: CellRenderer,
                            cellRendererParams: { colId: this.propWidget.columns[datasetColumn].id, propWidget: this.propWidget }
                        } as any

                        if (tempCol.measure === 'MEASURE') tempCol.aggregationSelected = this.propWidget.columns[datasetColumn].aggregation

                        //ROWSPAN MANAGEMENT
                        if (this.propWidget.settings.configuration.rows.rowSpan.enabled && this.propWidget.settings.configuration.rows.rowSpan.column === this.propWidget.columns[datasetColumn].id) {
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
                                            }),
                                            propWidget: this.propWidget
                                        }
                                    }
                                } else {
                                    // rows that are not pinned don't use any cell renderer
                                    return undefined
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

                        // TOOLTIP CONFIGURATION  -----------------------------------------------------------------
                        var tooltipConfig = this.getColumnTooltipConfig(tempCol.colId)
                        if (tooltipConfig !== null) {
                            tempCol.tooltipComponent = TooltipRenderer
                            tempCol.tooltipField = tempCol.field
                            tempCol.headerTooltip = tooltipConfig.header.enabled ? tooltipConfig.header.text : null
                            tempCol.tooltipComponentParams = { tooltipConfig: tooltipConfig }
                        } else {
                            tempCol.headerTooltip = null
                        }

                        // CUSTOM MESSAGE CONFIGURATION  -----------------------------------------------------------------
                        var customMessageConfig = this.propWidget.settings.configuration.customMessages
                        if (customMessageConfig) {
                            if (customMessageConfig.hideNoRowsMessage) this.gridApi.hideOverlay()
                            if (customMessageConfig.noRowsMessage) this.overlayNoRowsTemplateTest = customMessageConfig.noRowsMessage
                        }

                        // COLUMN GROUPING -----------------------------------------------------------------
                        var group = this.getColumnGroup(this.propWidget.columns[datasetColumn])
                        if (group) {
                            if (typeof columnGroups[group.id] != 'undefined') {
                                columns[columnGroups[group.id]].children.push(tempCol)
                            } else {
                                columnGroups[group.id] = columns.length
                                columns.push({
                                    colId: group.id,
                                    headerName: group.label,
                                    headerGroupComponent: HeaderGroupRenderer,
                                    headerGroupComponentParams: { colId: group.id, propWidget: this.propWidget },
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
        getWidgetStyleString() {
            const styleString = getWidgetStyleByType(this.propWidget, 'shadows') + getWidgetStyleByType(this.propWidget, 'padding') + getWidgetStyleByType(this.propWidget, 'borders')
            return styleString
        },
        getColumnTooltipConfig(colId) {
            var tooltipConfig = this.propWidget.settings.tooltips
            var columntooltipConfig = null as any
            tooltipConfig[0].enabled ? (columntooltipConfig = tooltipConfig[0]) : ''
            tooltipConfig.forEach((config) => {
                config.target.includes(colId) ? (columntooltipConfig = config) : ''
            })

            return columntooltipConfig
        },
        getRowStyle(params) {
            var rowStyles = this.propWidget.settings.style.rows

            if (this.propWidget.settings.conditionalStyles.enabled) {
                for (let i = 0; i < Object.entries(params.data).length; i++) {
                    var element = Object.entries(params.data)[i]
                    if (element[0].includes('column_')) {
                        var conditionalColumnStyle = getColumnConditionalStyles(this.propWidget, this.propWidget.columns[i].id!, element[1], false)
                        if (conditionalColumnStyle) return conditionalColumnStyle
                    }
                }
            }

            if (rowStyles.alternatedRows && rowStyles.alternatedRows.enabled) {
                if (rowStyles.alternatedRows.oddBackgroundColor && params.node.rowIndex % 2 === 0) {
                    return { background: rowStyles.alternatedRows.oddBackgroundColor }
                }
                if (rowStyles.alternatedRows.evenBackgroundColor && params.node.rowIndex % 2 != 0) {
                    return { background: rowStyles.alternatedRows.evenBackgroundColor }
                }
            }
        },
        getColumnVisibilityCondition(colId) {
            var visCond = this.propWidget.settings.visualization.visibilityConditions
            var columnHidden = false as boolean

            if (visCond.enabled) {
                var colConditions = visCond.conditions.filter((condition) => condition.target.includes(colId))
                //We always take the 1st condition as a priority for the column and use that one.
                if (colConditions[0]) {
                    if (colConditions[0].condition.type === 'always') {
                        columnHidden = colConditions[0].hide
                    } else {
                        isConditionMet(colConditions[0].condition, colConditions[0].condition.variableValue) ? (columnHidden = colConditions[0].hide) : ''
                    }
                }
            }

            return columnHidden
        }
    }
})
</script>
<style lang="scss">
.cell-span {
    border-left: 1px solid lightgrey !important;
    border-right: 1px solid lightgrey !important;
    border-bottom: 1px solid lightgrey !important;
}
</style>
