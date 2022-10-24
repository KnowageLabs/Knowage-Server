<template>
    <div class="kn-table-widget-container p-d-flex p-d-row kn-flex">
        {{ multiSelectedCells }}
        <div v-if="selectedColumn" class="multiselect-overlay">
            <i class="fas fa-play kn-cursor-pointer" @click="applyMultiSelection" />
            values:{{ multiSelectedCells }}
        </div>

        <ag-grid-vue class="kn-table-widget-grid ag-theme-alpine kn-flex" :gridOptions="gridOptions"></ag-grid-vue>
        <PaginatorRenderer v-if="showPaginator" :propWidgetPagination="propWidget.settings.pagination" @pageChanged="$emit('pageChanged')" />
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { AxiosResponse } from 'axios'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import { IDataset, ISelection, IWidget } from '../../Dashboard'
import { defineComponent, PropType } from 'vue'
import { emitter } from '../../DashboardHelpers'
import { getWidgetStyleByType, getColumnConditionalStyles, isConditionMet, formatModelForGet } from './TableWidgetHelper'
import mainStore from '../../../../../App.store'
import dashboardStore from '../../Dashboard.store'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import 'ag-grid-community/styles/ag-grid.css' // Core grid CSS, always needed
import 'ag-grid-community/styles/ag-theme-alpine.css' // Optional theme CSS
import CellRenderer from './CellRenderer.vue'
import HeaderRenderer from './HeaderRenderer.vue'
import TooltipRenderer from './TooltipRenderer.vue'
import SummaryRowRenderer from './SummaryRowRenderer.vue'
import HeaderGroupRenderer from './HeaderGroupRenderer.vue'
import PaginatorRenderer from './PaginatorRenderer.vue'
import { getSelectorWidgetData, getWidgetData } from '../../DataProxyHelper'
import { updateStoreSelections } from '../interactionsHelpers/InteractionHelper'
import { mapActions } from 'pinia'
import store from '../../Dashboard.store'

export default defineComponent({
    name: 'table-widget',
    emits: ['pageChanged', 'launchSelection'],
    components: { AgGridVue, HeaderRenderer, SummaryRowRenderer, HeaderGroupRenderer, TooltipRenderer, PaginatorRenderer },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        editorMode: { type: Boolean, required: false },
        datasets: { type: Array as any, required: true },
        dataToShow: { type: Object as any, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    watch: {
        propWidget: {
            handler() {
                if (!this.editorMode) this.createDatatableColumns()
            },
            deep: true
        },
        dataToShow: {
            handler() {
                console.log('%c Table DataToShow ---------------------', 'background-color: #2C2F33; color: white')
                console.log(this.dataToShow)
                this.tableData = this.dataToShow
                this.createDatatableColumns()
                // this.updateData(this.tableData.rows)
                this.loadActiveSelectionValue()
            },
            deep: true
        },
        propActiveSelections() {
            this.loadActiveSelections()
        }
    },
    computed: {
        gridWidth(): any {
            return (this.store.$state as any).user.sessionRole
        }
    },
    data() {
        return {
            descriptor,
            gridOptions: null as any,
            columnsNameArray: [] as any,
            rowData: [] as any,
            columnDefs: [] as any,
            gridApi: null as any,
            columnApi: null as any,
            overlayNoRowsTemplateTest: null as any,
            tableData: [] as any,
            showPaginator: false,
            activeSelections: [] as ISelection[],
            multiSelectedCells: [] as any,
            selectedRows: [] as any,
            selectedColumn: false as any
        }
    },
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    created() {
        this.setEventListeners()
        this.loadActiveSelections()
        this.setupDatatableOptions()
        this.loadActiveSelectionValue()
        this.tableData = this.dataToShow
    },
    unmounted() {
        this.removeEventListeners()
    },
    mounted() {},

    methods: {
        ...mapActions(store, ['setSelections']),
        setEventListeners() {
            // emitter.on('paginationChanged', (pagination) => console.log('WidgetEditorPreview - PAGINATION CHANGED!', pagination)) //  { enabled: this.paginationEnabled, itemsNumber: +this.itemsNumber }
            // emitter.on('sortingChanged', this.sortColumn)
            // emitter.on('refreshTable', this.createDatatableColumns)
            emitter.on('selectionsDeleted', this.onSelectionsDeleted)
        },
        removeEventListeners() {
            // emitter.off('refreshTable', this.createDatatableColumns)
            emitter.off('selectionsDeleted', this.onSelectionsDeleted)
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
        },
        loadActiveSelectionValue() {
            if (this.editorMode) return false
            const index = this.activeSelections.findIndex((selection: ISelection) => selection.datasetId === this.propWidget.dataset && selection.columnName === this.propWidget.columns[0]?.columnName)
            if (index !== -1) {
                const modalSelection = this.propWidget.settings.interactions.selection
                const selection = this.activeSelections[index]
                if (modalSelection.multiselection.enabled) {
                    // TODO - See about selected column
                    this.multiSelectedCells = selection.value
                }
            }
        },
        setupDatatableOptions() {
            this.gridOptions = {
                // PROPERTIES
                rowData: this.rowData,
                columnDefs: this.columnDefs,
                tooltipShowDelay: 100,
                tooltipMouseTrack: true,
                overlayNoRowsTemplate: this.overlayNoRowsTemplateTest,
                defaultColDef: { flex: 1 },
                rowSelection: 'single',
                suppressRowTransform: true,
                suppressMovableColumns: true,
                suppressDragLeaveHidesColumns: true,
                suppressRowGroupHidesColumns: true,
                rowHeight: 25,

                // EVENTS
                onCellClicked: this.onCellClicked,

                // CALLBACKS
                onGridReady: this.onGridReady,
                getRowStyle: this.getRowStyle
            }
        },
        onGridReady(params) {
            this.gridApi = params.api
            this.columnApi = params.columnApi

            this.createDatatableColumns()
        },
        async createDatatableColumns() {
            const datatableColumns = this.getTableColumns(this.tableData?.metaData?.fields)
            this.toggleHeaders(this.propWidget.settings.configuration.headers)
            this.gridApi.setColumnDefs(datatableColumns)
            this.updateData(this.tableData?.rows)
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
                            columnName: this.propWidget.columns[datasetColumn].columnName,
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
                            var tempRows = this.tableData.rows as any
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
                                'cell-span': function(params) {
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

                        // PAGINATION CONFIGURATION  -----------------------------------------------------------------
                        var pagination = this.propWidget.settings.pagination
                        if (pagination.enabled) {
                            this.showPaginator = true
                        } else this.showPaginator = false

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
            var rowData = Object.entries(params.data).filter((row) => row[0].includes('column_'))
            if (this.propWidget.settings.conditionalStyles.enabled) {
                for (let i = 0; i < rowData.length; i++) {
                    var conditionalColumnStyle = getColumnConditionalStyles(this.propWidget, this.propWidget.columns[i].id!, rowData[i][1], false)
                    if (conditionalColumnStyle) return conditionalColumnStyle
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
        },
        updateData(data) {
            console.log('%c UPDATE DATA ---------------------', 'background-color: #2C2F33; color: green')
            console.log(data)
            if (this.propWidget.settings.configuration.summaryRows.enabled) {
                var rowsNumber = this.propWidget.settings.configuration.summaryRows.list.length
                this.gridApi.setRowData(data.slice(0, data.length - rowsNumber))
                this.gridApi.setPinnedBottomRowData(data.slice(-rowsNumber))
            } else {
                this.gridApi.setRowData(data)
                this.gridApi.setPinnedBottomRowData()
            }
        },
        onCellClicked(node) {
            if (node.colDef.measure == 'MEASURE' || node.colDef.pinned || node.value === '' || node.value == undefined) return

            var modalSelection = this.propWidget.settings.interactions.selection
            if (modalSelection.enabled) {
                if (modalSelection.multiselection.enabled) {
                    //first check to see it the column selected is the same, if not clear the past selections
                    if (!this.selectedColumn || this.selectedColumn != node.colDef.field) {
                        this.multiSelectedCells.splice(0, this.multiSelectedCells.length)
                        this.selectedColumn = node.colDef.field
                    }

                    if (modalSelection.modalColumn) {
                        const modalColumnIndex = this.propWidget.columns.findIndex((column) => column.id == modalSelection.modalColumn)
                        const modalColumnValue = node.data[`column_${modalColumnIndex + 1}`]

                        if (!this.multiSelectedCells.includes(modalColumnValue)) this.multiSelectedCells.push(modalColumnValue)
                        else this.multiSelectedCells.splice(this.multiSelectedCells.indexOf(modalColumnValue), 1)
                        if (this.multiSelectedCells.length == 0) this.selectedColumn = false
                    } else {
                        if (!this.multiSelectedCells.includes(node.value)) this.multiSelectedCells.push(node.value)
                        else this.multiSelectedCells.splice(this.multiSelectedCells.indexOf(node.value), 1)
                        if (this.multiSelectedCells.length == 0) this.selectedColumn = false
                    }

                    // console.log('SELECTED CELLS -----------', this.multiSelectedCells)
                } else if (!modalSelection.multiselection.enabled) {
                    if (modalSelection.modalColumn) {
                        const modalColumnIndex = this.propWidget.columns.findIndex((column) => column.id == modalSelection.modalColumn)
                        const modalColumnValue = node.data[`column_${modalColumnIndex + 1}`]

                        // console.log('SINGLESELECT MODAL ', this.createNewSelection([modalColumnValue], this.propWidget.columns[modalColumnIndex].columnName))
                        updateStoreSelections(this.createNewSelection([modalColumnValue], this.propWidget.columns[modalColumnIndex].columnName), this.activeSelections, this.dashboardId, this.setSelections, this.$http)
                    } else {
                        // console.log('SINGLESELECT NO MODAL', this.createNewSelection([node.value], node.colDef.columnName))
                        updateStoreSelections(this.createNewSelection([node.value], node.colDef.columnName), this.activeSelections, this.dashboardId, this.setSelections, this.$http)
                    }
                }
            }
        },
        applyMultiSelection() {
            const modalSelection = this.propWidget.settings.interactions.selection

            let tempSelection = null as ISelection | null
            if (modalSelection.modalColumn) {
                const modalColumnIndex = this.propWidget.columns.findIndex((column) => column.id == modalSelection.modalColumn)
                const modalColumnName = this.propWidget.columns[modalColumnIndex].columnName
                tempSelection = this.createNewSelection(this.multiSelectedCells, modalColumnName)
            } else {
                const columnIndex = this.selectedColumn?.split('_')[1]
                if (columnIndex || columnIndex === 0) tempSelection = this.createNewSelection(this.multiSelectedCells, this.propWidget.columns[columnIndex - 1].columnName)
            }
            if (tempSelection) {
                this.updateActiveSelectionsWithMultivalueSelection(tempSelection)
                this.$emit('launchSelection')
            }
        },

        updateActiveSelectionsWithMultivalueSelection(tempSelection: ISelection) {
            const index = this.activeSelections.findIndex((activeSelection: ISelection) => activeSelection.datasetId === tempSelection.datasetId && activeSelection.columnName === tempSelection.columnName)
            if (index !== -1) {
                this.activeSelections[index] = tempSelection
            } else {
                this.activeSelections.push(tempSelection)
            }
        },
        mapRow(rowData) {
            var keyMap = {}
            for (var r in rowData) {
                for (var f in this.tableData?.metaData?.fields) {
                    if (this.tableData?.metaData?.fields[f].dataIndex == r) keyMap[this.tableData?.metaData?.fields[f].header] = rowData[r]
                }
            }
            return keyMap
        },
        createNewSelection(value: (string | number)[], columnName: string) {
            return { datasetId: this.propWidget.dataset as number, datasetLabel: this.getDatasetLabel(this.propWidget.dataset as number), columnName: columnName, value: value, aggregated: false, timestamp: new Date().getTime() }
        },
        getDatasetLabel(datasetId: number) {
            const index = this.datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
            return index !== -1 ? this.datasets[index].label : ''
        },
        onSelectionsDeleted(selections: any) {
            const index = selections.findIndex((selection: ISelection) => selection.datasetId === this.propWidget.dataset && selection.columnName === this.propWidget.columns[0]?.columnName)
            if (index !== -1) this.removeSelectedValues()
        },
        removeSelectedValues() {
            this.multiSelectedCells = []
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
.multiselect-overlay {
    font-size: 0.8rem;
    background-color: #f1f5f9;
    min-height: 25px;
    max-height: 25px;
    border-top: 1px solid #3b678c;
    border-bottom: 1px solid #3b678c;
    text-align: center;
    width: 100%;
    z-index: 9999;
    opacity: 0.7;
}
</style>
