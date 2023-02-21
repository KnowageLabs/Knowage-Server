<template>
    <div id="registry-gric-container" class="kn-height-full p-d-flex p-flex-column">
        <div id="registry-grid-toolbar" class="p-d-flex p-flex-row p-ai-center" :style="registryDescriptor.styles.tableToolbar">
            <div v-if="selectedRows.length > 0" class="p-ml-1">{{ selectedRows.length }} {{ $t('documentExecution.registry.grid.rowsSelected') }}</div>
            <div id="operation-buttons-containter" class="p-ml-auto" :style="registryDescriptor.styles.tableToolbarButtonContainer">
                <Button icon="fas fa-plus" class="p-button-text p-button-rounded p-button-plain kn-button-light" v-tooltip.top="$t('documentExecution.registry.grid.addRow')" data-test="new-row-button" @click="addNewRow" />
                <Button icon="fas fa-clone" class="p-button-text p-button-rounded p-button-plain kn-button-light" v-tooltip.top="$t('documentExecution.registry.grid.cloneRows')" @click="cloneRows" />
                <Button icon="fas fa-trash" class="p-button-text p-button-rounded p-button-plain kn-button-light" v-tooltip.top="$t('documentExecution.registry.grid.deleteRows')" @click="rowsDeleteConfirm()" />
            </div>
            <Button icon="fas fa-save" class="p-button-text p-button-rounded p-button-plain kn-button-light" @click="$emit('saveRegistry')" />
        </div>
        <ag-grid-vue v-if="!loading" class="registry-grid ag-theme-alpine kn-height-full" :rowData="rows" :gridOptions="gridOptions" :context="context" />
    </div>

    <RegistryDatatableWarningDialog :visible="warningVisible" :columns="dependentColumns" @close="onWarningDialogClose"></RegistryDatatableWarningDialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { luxonFormatDate, formatDateWithLocale, localeDate, primeVueDate, getLocale } from '@/helpers/commons/localeHelper'
import { setInputDataType, formatRegistryNumber } from '@/helpers/commons/tableHelpers'
import { AxiosResponse } from 'axios'
import { mapActions } from 'pinia'
import { emitter } from './RegistryDatatableHelper'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import 'ag-grid-community/styles/ag-grid.css' // Core grid CSS, always needed
import 'ag-grid-community/styles/ag-theme-alpine.css' // Optional theme CSS
import registryDescriptor from '../RegistryDescriptor.json'
import RegistryDatatableWarningDialog from './RegistryDatatableWarningDialog.vue'
import CellEditor from './registryCellRenderers/RegistryCellEditor.vue'
import HeaderRenderer from './registryCellRenderers/RegistryHeaderRenderer.vue'
import TooltipRenderer from './registryCellRenderers/RegistryTooltipRenderer.vue'
import store from '../../../../App.store'
import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'registry-datatable',
    components: { RegistryDatatableWarningDialog, AgGridVue, HeaderRenderer, TooltipRenderer },
    props: {
        propColumns: { type: Array },
        propRows: { type: Array, required: true },
        columnMap: { type: Object },
        propConfiguration: { type: Object },
        pagination: { type: Object },
        entity: { type: Object as PropType<String | null> },
        id: { type: String },
        stopWarningsState: { type: Array },
        dataLoading: { type: Boolean }
    },
    emits: ['rowChanged', 'rowDeleted', 'pageChanged', 'warningChanged', 'saveRegistry', 'sortingChanged'],
    data() {
        return {
            registryDescriptor,
            columns: [] as any[],
            rows: [] as any[],
            configuration: {} as any,
            comboColumnOptions: {} as any,
            buttons: {
                enableButtons: false,
                enableDeleteRecords: false,
                enableAddRecords: false
            },
            lazyParams: {} as any,
            dependentColumns: [] as any[],
            selectedRow: null as any,
            warningVisible: false,
            stopWarnings: [] as any[],
            flagShown: 'flag-shown',
            flagHidden: 'flag-hidden',
            first: 0,
            loading: false,
            multiSortMeta: [],
            gridApi: null as any,
            columnApi: null as any,
            timeout: null as any,
            selectedRows: [] as any,
            gridOptions: null as any,
            context: null as any,
            ctrlDown: false,
            sortModel: {
                fieldName: '',
                orderType: 'NONE'
            }
        }
    },
    computed: {
        getCurrentLocaleDefaultDateFormat() {
            return (column) => (column.isEditable ? column.format || primeVueDate() : localeDate())
        }
    },
    watch: {
        propColumns() {
            this.loadColumnDefinitions()
        },
        propConfiguration() {
            this.loadConfiguration()
        },
        dataLoading() {
            this.dataLoading ? this.gridApi.showLoadingOverlay() : this.gridApi.hideOverlay()
        },
        pagination: {
            handler() {
                this.loadPagination()
                this.first = this.pagination?.start
            },
            deep: true
        }
    },
    beforeMount() {
        this.context = { componentParent: this }
    },
    created() {
        this.setEventListeners()
        this.loadColumnDefinitions()
        this.loadRows()
        this.loadConfiguration()
        this.loadPagination()
        this.loadWarningState()
        this.setupDatatableOptions()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        ...mapActions(store, ['setInfo', 'setError']),
        setEventListeners() {
            emitter.on('refreshTableWithData', this.loadRows)
        },
        removeEventListeners() {
            emitter.off('refreshTableWithData', this.loadRows)
        },
        onGridReady(params) {
            this.gridApi = params.api
            this.columnApi = params.columnApi

            this.refreshGridConfiguration()
        },
        refreshGridConfiguration() {
            this.gridApi.setColumnDefs(this.columns)
            this.gridApi.setRowData(this.rows)
        },
        setupDatatableOptions() {
            this.gridOptions = {
                // PROPERTIES
                columnDefs: this.columns,
                tooltipShowDelay: 100,
                tooltipMouseTrack: true,
                rowHeight: 35,
                defaultColDef: {
                    editable: false,
                    enableValue: true,
                    sortable: true,
                    resizable: true,
                    width: 100,
                    tooltipComponent: TooltipRenderer,
                    cellClassRules: {
                        'edited-cell-color-class': (params) => {
                            if (params.data.isEdited) return params.data.isEdited.includes(params.colDef.field)
                        }
                    }
                },
                rowSelection: 'multiple',
                animateRows: true,
                suppressScrollOnNewData: true,

                // EVENTS
                onCellKeyDown: this.onCellKeyDown,
                onBodyScroll: this.onBodyScroll,
                onSelectionChanged: this.onSelectionChanged,
                onCellValueChanged: this.onCellValueChanged,

                // CALLBACKS
                onGridReady: this.onGridReady,
                // onSortChanged: this.onSortChanged1,
                getRowStyle: this.getRowStyle,
                getRowId: this.getRowId
            }
        },

        async loadColumnDefinitions() {
            this.loading = true
            this.columns = [
                {
                    colId: 'indexColumn',
                    valueGetter: `node.rowIndex + 1`,
                    headerName: 'id',
                    pinned: 'left',
                    isVisible: true,
                    isEditable: false,
                    suppressMovable: true,
                    resizable: false,
                    columnInfo: { type: 'int' },
                    cellStyle: (params) => {
                        return { color: 'black', backgroundColor: registryDescriptor.styles.colors.disabledCellColor, opacity: 0.8 }
                    }
                }
            ]
            this.propColumns?.forEach((el: any) => {
                if (el.isVisible) {
                    el.editable = el.isEditable
                    el.headerName = el.title ?? el.columnInfo.header
                    el.tooltipField = el.field

                    this.addColumnEditableProps(el)
                    this.addColumnCheckboxRendererProps(el)
                    this.addColumnFormattingProps(el)

                    el.headerComponent = HeaderRenderer
                    el.headerComponentParams = {
                        sortModel: this.sortModel
                    }

                    this.columns.push(el)
                }
            })
            this.setColumnDependencies()
            await this.loadInitialDropdownOptions()
            this.loading = false
        },
        addColumnEditableProps(el: any) {
            if (el.editable) {
                el.cellEditor = CellEditor
                el.cellEditorParams = {
                    comboColumnOptions: this.comboColumnOptions
                }
            } else {
                el.cellStyle = (params) => {
                    return { color: 'black', backgroundColor: 'rgba(231, 231, 231, 0.8)', opacity: 0.8 }
                }
            }
        },
        addColumnCheckboxRendererProps(el) {
            if (el.editorType == 'TEXT' && el.columnInfo.type === 'boolean') {
                el.cellRenderer = (params) => {
                    return `<i class="fas fa-${params.value ? 'check' : 'times'}"/>`
                }
            }
        },
        addColumnFormattingProps(el: any) {
            let locale = getLocale()
            locale = locale ? locale.replace('_', '-') : ''
            if (el.columnInfo?.type === 'date') {
                el.valueFormatter = (params) => {
                    return this.getFormattedDate(params.value, 'yyyy-MM-dd', this.getCurrentLocaleDefaultDateFormat(el))
                }
            } else if (el.columnInfo?.type === 'timestamp') {
                el.valueFormatter = (params) => {
                    return this.getFormattedDateTime(params.value, { dateStyle: 'short', timeStyle: 'medium' }, true)
                }
            } else if (['int', 'float', 'decimal', 'long'].includes(el.columnInfo.type)) {
                el.valueFormatter = (params: any) => {
                    let configuration = { useGrouping: false, minFractionDigits: 0, maxFractionDigits: 0 } as { useGrouping: boolean; minFractionDigits: number; maxFractionDigits: number } | null
                    configuration = formatRegistryNumber(el)
                    return Intl.NumberFormat(locale, { useGrouping: configuration?.useGrouping, minimumFractionDigits: configuration?.minFractionDigits, maximumFractionDigits: configuration?.maxFractionDigits ?? 2 }).format(params.value)
                }
            }
        },
        setColumnDependencies() {
            this.columns.forEach((column: any) => {
                if (column.dependences) {
                    const index = this.columns.findIndex((parentColumn: any) => parentColumn.field === column.dependences)
                    if (index !== -1) {
                        this.columns[index].hasDependencies ? this.columns[index].hasDependencies.push(column) : (this.columns[index].hasDependencies = [column])
                        this.comboColumnOptions[column.dependences] = []
                    }
                }
            })
        },
        async loadInitialDropdownOptions() {
            for (let i = 0; i < this.columns.length; i++) {
                if (this.columns[i].editorType === 'COMBO') {
                    await this.addColumnOptions({ column: this.columns[i], row: {} })
                }
            }
        },
        async addColumnOptions(payload: any) {
            const column = payload.column
            const row = payload.row

            if (!this.comboColumnOptions[column.field]) {
                this.comboColumnOptions[column.field] = []
            }

            if (!this.comboColumnOptions[column.field][row[column.dependences]]) {
                await this.loadColumnOptions(column, row)
            }
        },
        async loadColumnOptions(column: any, row: any) {
            this.gridApi?.showLoadingOverlay()
            const subEntity = column.subEntity ? '::' + column.subEntity + '(' + column.foreignKey + ')' : ''

            const entityId = this.entity + subEntity + ':' + column.field
            const entityOrder = this.entity + subEntity + ':' + (column.orderBy ?? column.field)

            const postData = new URLSearchParams({
                ENTITY_ID: entityId,
                QUERY_TYPE: 'standard',
                ORDER_ENTITY: entityOrder,
                ORDER_TYPE: 'asc',
                QUERY_ROOT_ENTITY: 'true'
            })
            if (column.dependences && row && row[column.dependences]) {
                postData.append('DEPENDENCES', this.entity + subEntity + ':' + column.dependences + '=' + row[column.dependences])
            }
            await this.$http
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then((response: AxiosResponse<any>) => (this.comboColumnOptions[column.field][row[column.dependences] ?? 'All'] = response.data.rows))
            this.gridApi?.hideOverlay()
        },
        loadConfiguration() {
            this.configuration = this.propConfiguration

            for (let i = 0; i < this.configuration.length; i++) {
                if (this.configuration[i].name === 'enableButtons') {
                    this.buttons.enableButtons = this.configuration[i].value === 'true'
                } else {
                    if (this.configuration[i].name === 'enableDeleteRecords') {
                        this.buttons.enableDeleteRecords = this.configuration[i].value === 'true'
                    }
                    if (this.configuration[i].name === 'enableAddRecords') {
                        this.buttons.enableAddRecords = this.configuration[i].value === 'true'
                    }
                }
            }
        },
        loadRows() {
            this.rows = this.propRows
            this.gridApi?.setRowData(this.rows)
        },
        getRowId(params) {
            return params.data.uniqueId
        },
        loadPagination() {
            this.lazyParams = { ...this.pagination } as any
        },
        loadWarningState() {
            this.stopWarnings = this.stopWarningsState as any[]
        },
        onPage(event: any) {
            this.lazyParams = {
                paginationStart: event.first,
                paginationLimit: event.rows,
                paginationEnd: event.first + event.rows,
                size: this.lazyParams.size
            }
            this.$emit('pageChanged', this.lazyParams)
        },
        rowsDeleteConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteRows()
            })
        },
        deleteRows() {
            var rowsForTableDeletion = this.selectedRows.filter((row) => row.isNew)
            if (rowsForTableDeletion.length > 0) {
                rowsForTableDeletion.forEach((val) => {
                    var foundIndex = this.rows.indexOf(val)
                    if (foundIndex != -1) this.rows.splice(foundIndex, 1)
                })
                this.gridApi.applyTransaction({ remove: rowsForTableDeletion })
            }

            var rowsForServiceDeletion = this.selectedRows.filter((row) => !row.isNew)
            if (rowsForServiceDeletion.length > 0) this.$emit('rowDeleted', rowsForServiceDeletion)
        },
        getFormattedDate(date: any, format: any, incomingFormat?: string) {
            return luxonFormatDate(date, format, incomingFormat)
        },
        getFormattedDateTime(date: any, format?: any, keepNull?: boolean) {
            return formatDateWithLocale(date, format, keepNull)
        },
        addNewRow() {
            const newRow = { uniqueId: cryptoRandomString({ length: 16, type: 'base64' }), id: this.rows.length + 1, isNew: true }
            this.columns.forEach((el: any) => {
                if (el.isVisible && el.field !== 'id') {
                    newRow[el.field] = el.defaultValue ?? ''
                }
            })
            this.addRowToFirstPosition(newRow)

            if (this.lazyParams.size <= registryDescriptor.paginationLimit) {
                this.first = 0
            }
            this.$emit('rowChanged', newRow)
        },
        cloneRows() {
            this.selectedRows.forEach((row) => {
                const tempRow = { ...row }
                tempRow.uniqueId = cryptoRandomString({ length: 16, type: 'base64' })
                tempRow.isNew = true
                delete tempRow.id
                this.addRowToFirstPosition(tempRow)
            })
        },
        addRowToFirstPosition(newRow: any) {
            this.rows.unshift(newRow)
            this.gridApi.applyTransaction({ addIndex: 0, add: [newRow] })
        },
        onDropdownChange(payload: any) {
            const column = payload.column
            const row = payload.row

            this.selectedRow = row
            if (column.hasDependencies) {
                this.dependentColumns = [] as any[]
                this.setDependentColumns(column)
                if (!this.stopWarnings[column.field]) {
                    this.dependentColumns.forEach((el: any) => {
                        if (this.selectedRow[el.field]) this.warningVisible = true
                    })
                } else this.clearDependentColumnsValues()
            }

            row.edited = true
            this.$emit('rowChanged', row)
        },
        setDependentColumns(column: any) {
            let tempColumn = column
            if (!tempColumn.hasDependencies) return

            tempColumn.hasDependencies.forEach((el: any) => {
                this.dependentColumns.push(el)
                this.setDependentColumns(el)
            })
        },
        onWarningDialogClose(payload: any) {
            if (payload.stopWarnings) {
                this.stopWarnings[payload.columnField] = true
                this.$emit('warningChanged', this.stopWarnings)
            }

            this.clearDependentColumnsValues()
            this.warningVisible = false
        },
        clearDependentColumnsValues() {
            this.dependentColumns.forEach((el: any) => (this.selectedRow[el.field] = ''))
            this.selectedRow.edited = true
            this.$emit('rowChanged', this.selectedRow)
            this.gridApi.refreshCells()
        },
        setRowEdited(row: any) {
            row.edited = true
            this.$emit('rowChanged', row)
        },
        onCellEditComplete(event: any) {
            let id = event.newData.id
            if (id) {
                var foundIndex = this.rows.findIndex((x) => x.id == id)
                this.rows[foundIndex] = event.newData
            }
        },
        onSelectionChanged() {
            this.selectedRows = this.gridApi.getSelectedRows()
        },
        onBodyScroll() {
            if (this.timeout) clearTimeout(this.timeout)
            this.timeout = setTimeout(() => {
                var bottom_px = this.gridApi.getVerticalPixelRange().bottom
                var grid_height = this.gridApi.getDisplayedRowCount() * this.gridApi.getSizesForCurrentTheme().rowHeight
                if (bottom_px == grid_height) {
                    var newPaginationStart = this.lazyParams.start + this.registryDescriptor.paginationNumberOfItems
                    this.lazyParams = {
                        paginationStart: newPaginationStart,
                        paginationLimit: this.registryDescriptor.paginationLimit,
                        size: this.lazyParams.size
                    }

                    this.$emit('pageChanged', this.lazyParams)
                }
            }, 300)
        },
        async onCellKeyDown(ev) {
            const myCell = this.getFocusedCell(ev)
            const [ctrlKey, cKey, vKey] = [17, 67, 86]

            if (ev.event.which === ctrlKey) {
                this.ctrlDown = true
            } else if (ev.event.which == cKey && this.ctrlDown == true) {
                window.navigator.clipboard.writeText(ev.value).catch((er) => console.log(er))
            } else if (ev.event.which == vKey && this.ctrlDown == true) {
                await window.navigator.clipboard.readText().then(async (value) => {
                    await this.setCellValue(myCell, value)
                })
            }
        },
        getFocusedCell(ev) {
            const focusedCell = ev.api.getFocusedCell()
            const rowNode = ev.api.getRowNode(ev.data.uniqueId)
            const column = focusedCell.column.colDef.field
            return { cell: focusedCell, column: column, row: rowNode }
        },
        async setCellValue(selectedCell, pasteValue) {
            var colDef = selectedCell.cell.column.colDef
            var cellType = this.getCellType(colDef)

            if (this.cellAcceptsPasteValue(colDef, cellType)) {
                switch (cellType) {
                    case 'text':
                        selectedCell.row.setDataValue(selectedCell.column, pasteValue)
                        break
                    case 'number':
                        this.setNumbericCellValue(selectedCell, pasteValue)
                        break
                    case 'dropdown':
                        await this.setDropdownCellValue(colDef, selectedCell, pasteValue)
                        break
                    default:
                        break
                }
            }
        },
        setNumbericCellValue(selectedCell: any, pasteValue: any) {
            if (!isNaN(pasteValue)) {
                selectedCell.row.setDataValue(selectedCell.column, pasteValue)
            } else {
                this.setCannotPasteWarning('nan')
            }
        },
        async setDropdownCellValue(colDef: any, selectedCell: any, pasteValue: any) {
            await this.addColumnOptions({
                column: colDef,
                row: selectedCell.row.data
            })
            if (!this.validateDropdownValueAfterCopyPaste(colDef, pasteValue, selectedCell)) {
                this.setCannotPasteWarning('dropdown')
            } else {
                selectedCell.row.setDataValue(selectedCell.column, pasteValue)
                this.onDropdownChange({ row: selectedCell.row.data, column: selectedCell.cell.column.userProvidedColDef })
            }
        },
        validateDropdownValueAfterCopyPaste(colDef: any, pasteValue: string, selectedCell: any) {
            const parentCellValue = selectedCell.row.data[colDef.dependences]
            let options = this.comboColumnOptions && this.comboColumnOptions[colDef.field] ? this.comboColumnOptions[colDef.field][parentCellValue ?? 'All'] : []
            if (!options) return false
            const index = options.findIndex((dropdownOption: any) => dropdownOption['column_1'] === pasteValue)
            return index !== -1
        },
        cellAcceptsPasteValue(colDef, cellType) {
            if (colDef.editable == false || colDef.isEditable == false) {
                this.setCannotPasteWarning('notEditable')
                return false
            } else if (cellType === 'checkbox') {
                this.setCannotPasteWarning('checkbox')
                return false
            } else if (cellType === 'temporal') {
                this.setCannotPasteWarning('temporal')
                return false
            } else return true
        },
        setCannotPasteWarning(type: string) {
            let message = ''
            switch (type) {
                case 'notEditable':
                    message = this.$t('documentExecution.registry.copyPasteValidationErrors.notEditable')
                    break
                case 'dropdown':
                    message = this.$t('documentExecution.registry.copyPasteValidationErrors.dropdown')
                    break
                case 'checkbox':
                    message = this.$t('documentExecution.registry.copyPasteValidationErrors.checkbox')
                    break
                case 'temporal':
                    message = this.$t('documentExecution.registry.copyPasteValidationErrors.temporal')
                    break
                case 'nan':
                    message = 'NOT A NUMBER'
                    break
            }
            this.setInfo({ title: this.$t('common.error.generic'), msg: message })
        },
        getCellType(colDef) {
            if (colDef.editorType == 'TEXT' && colDef.columnInfo.type === 'boolean') return 'checkbox'
            if (colDef.editorType !== 'COMBO' && colDef.columnInfo?.type !== 'date' && colDef.columnInfo?.type !== 'timestamp' && setInputDataType(colDef.columnInfo?.type) === 'text') return 'text'
            if (colDef.editorType !== 'COMBO' && colDef.columnInfo?.type !== 'date' && colDef.columnInfo?.type !== 'timestamp' && setInputDataType(colDef.columnInfo?.type) === 'number') return 'number'
            if (colDef.editorType === 'COMBO') return 'dropdown'
            if (colDef.columnInfo?.type === 'date' || colDef.columnInfo?.type === 'timestamp') return 'temporal'
        },
        onCellValueChanged(params) {
            if (params.oldValue !== params.newValue) {
                if (params.data.isEdited) {
                    params.data.isEdited.push(params.colDef.field)
                } else params.data.isEdited = [params.colDef.field]
                this.$emit('rowChanged', params.data)
            }
            params.api.refreshCells()
        },
        getRowStyle(params) {
            if (params.data.isNew) return { 'background-color': registryDescriptor.styles.colors.newRowColor }
        },
        sortingChanged(updatedSortModel) {
            this.sortModel = updatedSortModel
            this.columns.forEach((el: any) => {
                if (el.isVisible) {
                    el.headerComponent = HeaderRenderer
                    el.headerComponentParams = {
                        sortModel: updatedSortModel
                    }
                }
            })
            this.refreshGridConfiguration()
            this.$emit('sortingChanged', updatedSortModel)
        }
    }
})
</script>
<style lang="scss">
.flag-shown {
    opacity: 1;
}
.flag-hidden {
    opacity: 0;
}
.registry-grid {
    border: none;
}
.edited-cell-color-class {
    background-color: #749e43;
}
</style>
