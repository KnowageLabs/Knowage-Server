<template>
    <div id="registry-gric-container" class="kn-height-full p-d-flex p-flex-column">
        <div class="registry-grid-toolbar">
            <Button icon="fas fa-plus" class="p-button-text p-button-rounded p-button-plain kn-button-light" v-tooltip.top="$t('documentExecution.registry.grid.addRow')" @click="addNewRow" />
            <Button icon="fas fa-clone" class="p-button-text p-button-rounded p-button-plain kn-button-light" v-tooltip.top="$t('documentExecution.registry.grid.cloneRows')" @click="cloneRows" />
            <Button icon="fas fa-trash" class="p-button-text p-button-rounded p-button-plain kn-button-light" v-tooltip.top="$t('documentExecution.registry.grid.deleteRows')" @click="rowsDeleteConfirm()" />
        </div>
        <ag-grid-vue v-if="!loading" class="registry-grid ag-theme-alpine" style="height: 100%" :rowData="rows" :gridOptions="gridOptions" :context="context" />
    </div>

    <!-- <DataTable
        v-if="!loading"
        class="p-datatable-sm kn-table"
        :scrollable="true"
        v-model:first="first"
        :value="rows"
        dataKey="id"
        paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
        :lazy="lazyParams.size > registryDescriptor.paginationLimit"
        :paginator="true"
        :rows="registryDescriptor.paginationNumberOfItems"
        :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
        :totalRecords="lazyParams.size"
        stripedRows
        showGridlines
        sortMode="multiple"
        :multiSortMeta="multiSortMeta"
        @page="onPage($event)"
        @sort="onSort"
    >
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <Column class="kn-truncated" :style="registryDatatableDescriptor.numerationColumn.style" :headerStyle="registryDatatableDescriptor.numerationColumn.style" :field="columns[0].field" :header="columns[0].title"></Column>

        <template v-for="col of columns.slice(1)" :key="col.field">
            <Column class="kn-truncated" :field="col.field" :style="`min-width:${col.size}px`" :sortable="col.columnInfo?.type !== 'timestamp' && col.columnInfo?.type !== 'date'">
                <template #header>
                    <div class="table-header">
                        <i v-if="showDefaultNumberFormatIcon(col)" v-tooltip.top="$t('documentExecution.registry.numberFormatNotSupported')" class="pi pi-exclamation-triangle kn-cursor-pointer"></i>
                        {{ col.title }}
                        <i v-if="col.isEditable && col.columnInfo?.type !== 'boolean'" class="pi pi-pencil edit-icon p-ml-2" :data-test="col.field + '-icon'" v-tooltip.bottom="$t('documentExecution.registry.isEditableField')" />
                    </div>
                </template>
                <template #body="slotProps">
                    <div class="p-d-flex p-flex-row editableField" :data-test="col.field + '-body'">
                        <Checkbox v-if="col.editorType == 'TEXT' && col.columnInfo?.type === 'boolean'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="setRowEdited(slotProps.data)" :disabled="!col.isEditable"></Checkbox>
                        <RegistryDatatableEditableField
                            v-else-if="col.isEditable || col.columnInfo?.type === 'int' || col.columnInfo?.type === 'float'"
                            :column="col"
                            :propRow="slotProps.data"
                            :comboColumnOptions="comboColumnOptions"
                            @rowChanged="setRowEdited(slotProps.data)"
                            @dropdownChanged="onDropdownChange"
                            @dropdownOpened="addColumnOptions"
                        ></RegistryDatatableEditableField>
                        <span v-else-if="!col.isEditable">
                            <span v-if="slotProps.data[col.field] && col.columnInfo?.type === 'date'">
                                {{ getFormattedDate(slotProps.data[col.field], 'yyyy-MM-dd', getCurrentLocaleDefaultDateFormat(col)) }}
                            </span>
                            <span v-else-if="slotProps.data[col.field] && col.columnInfo?.type === 'timestamp'"> {{ getFormattedDateTime(slotProps.data[col.field], { dateStyle: 'short', timeStyle: 'medium' }, true) }}</span>
                            <span v-else>{{ slotProps.data[col.field] }}</span>
                        </span>
                    </div>
                </template>
            </Column>
        </template>
        <Column :style="registryDatatableDescriptor.iconColumn.style" :headerStyle="registryDatatableDescriptor.iconColumn.style">
            <template #header>
                <Button class="kn-button" :label="$t('managers.businessModelManager.add')" v-if="buttons.enableButtons || buttons.enableAddRecords" @click="addNewRow" data-test="new-row-button" />
            </template>
            <template #body="slotProps">
                <Button v-if="buttons.enableButtons || buttons.enableDeleteRecords" class="p-button-link" @click="rowDeleteConfirm(slotProps.index, slotProps.data)">
                    <i class="pi pi-flag" :class="[slotProps.data.edited ? flagShown : flagHidden]" :style="registryDatatableDescriptor.primevueTableStyles.trashNormal" />
                    <i class="p-button-link pi pi-trash p-ml-2" :style="registryDatatableDescriptor.primevueTableStyles.trashNormal" />
                </Button>
            </template>
        </Column>
    </DataTable> -->
    <RegistryDatatableWarningDialog :visible="warningVisible" :columns="dependentColumns" @close="onWarningDialogClose"></RegistryDatatableWarningDialog>
</template>

<script lang="ts">
import { defineComponent, PropType, reactive, onMounted, ref } from 'vue'
import { luxonFormatDate, formatDateWithLocale, formatNumberWithLocale, localeDate, primeVueDate } from '@/helpers/commons/localeHelper'
import { setInputDataType, numberFormatRegex } from '@/helpers/commons/tableHelpers'
import { AxiosResponse } from 'axios'
import { mapActions } from 'pinia'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import 'ag-grid-community/styles/ag-grid.css' // Core grid CSS, always needed
import 'ag-grid-community/styles/ag-theme-alpine.css' // Optional theme CSS
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Paginator from 'primevue/paginator'
import registryDescriptor from '../RegistryDescriptor.json'
import registryDatatableDescriptor from './RegistryDatatableDescriptor.json'
import RegistryDatatableEditableField from './RegistryDatatableEditableField.vue'
import RegistryDatatableWarningDialog from './RegistryDatatableWarningDialog.vue'
import CellRenderer from './registryCellRenderers/RegistryCellRenderer.vue'
import CellEditor from './registryCellRenderers/RegistryCellEditor.vue'
import deepcopy from 'deepcopy'
import store from '../../../../App.store'
import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'registry-datatable',
    components: { Checkbox, Column, DataTable, RegistryDatatableEditableField, RegistryDatatableWarningDialog, AgGridVue, Paginator },
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
    emits: ['rowChanged', 'rowDeleted', 'pageChanged', 'warningChanged'],
    data() {
        return {
            registryDescriptor,
            registryDatatableDescriptor,
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
            ctrlDown: false
        }
    },
    watch: {
        propColumns() {
            this.loadColumns()
        },
        //TODO - load rows using MITT because reactivity is dumb af
        // propRows() {
        //     this.loadRows()
        // },
        propRows: {
            handler() {
                this.loadRows()
            },
            deep: true
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
        this.loadColumns()
        this.loadRows()
        this.loadConfiguration()
        this.loadPagination()
        this.loadWarningState()
        this.setupDatatableOptions()
    },
    computed: {
        getCurrentLocaleDefaultDateFormat() {
            return (column) => (column.isEditable ? column.format || primeVueDate() : localeDate())
        }
    },
    methods: {
        ...mapActions(store, ['setInfo', 'setError']),
        setupDatatableOptions() {
            this.gridOptions = {
                // PROPERTIES
                columnDefs: this.columns,
                tooltipShowDelay: 100,
                tooltipMouseTrack: true,
                defaultColDef: { editable: false, enableValue: true, sortable: true, resizable: true, width: 100 },
                rowSelection: 'multiple',
                animateRows: true,
                suppressScrollOnNewData: true,

                // EVENTS
                onCellClicked: this.cellWasClicked,
                onCellKeyDown: this.pasteTest,
                onBodyScroll: this.onBodyScroll,
                onSelectionChanged: this.onSelectionChanged,

                // CALLBACKS
                onGridReady: this.onGridReady
            }
        },
        async loadColumns() {
            this.loading = true
            this.columns = [
                {
                    colId: 'indexColumn',
                    valueGetter: `node.rowIndex + 1`,
                    headerName: 'id',
                    pinned: 'left',
                    isVisible: true,
                    isEditable: false,
                    columnInfo: { type: 'int' },
                    cellStyle: (params) => {
                        return { color: 'black', backgroundColor: 'rgba(231, 231, 231, 0.8)', opacity: 0.8 }
                    }
                }
            ]
            this.propColumns?.forEach((el: any) => {
                if (el.isVisible) {
                    console.log('column def', el)
                    // NOTE - Applying renderer here, so it could actually receive comboColumnOptions parameter that it needs, wont work in coldef
                    el.editable = el.isEditable

                    if (el.editable) {
                        el.cellEditor = CellEditor
                        el.cellEditorParams = { comboColumnOptions: this.comboColumnOptions }
                    } else {
                        el.cellStyle = (params) => {
                            return { color: 'black', backgroundColor: 'rgba(231, 231, 231, 0.8)', opacity: 0.8 }
                        }
                    }

                    if (el.editorType == 'TEXT' && el.columnInfo.type === 'boolean') {
                        el.cellRenderer = (params) => {
                            return `<input  type='checkbox' ${params.value ? 'checked' : ''} />`
                        }
                    }

                    // TODO - Formatting logic for dates, not working when editing date
                    if (el.columnInfo?.type === 'date') {
                        el.valueFormatter = (params) => {
                            this.getFormattedDate(params.value, 'yyyy-MM-dd', this.getCurrentLocaleDefaultDateFormat(el))
                        }
                    } else if (el.columnInfo?.type === 'timestamp') {
                        el.valueFormatter = (params) => {
                            this.getFormattedDateTime(params.value, { dateStyle: 'short', timeStyle: 'medium' }, true)
                        }
                    }

                    this.columns.push(el)
                }
            })
            this.setColumnDependencies()
            await this.loadInitialDropdownOptions()
            this.loading = false
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
        },
        loadRows() {
            this.rows = this.propRows
            // this.gridApi?.setRowData(this.rows)
            console.log('PROP ROWS -----------------', this.rows)
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
            //TODO - check for newRows, if there are any, splice them first, then emit old rows for deletion
            // row.isNew ? this.rows.splice(index, 1) : this.$emit('rowDeleted', row)
            this.$emit('rowDeleted', this.selectedRows)
        },

        //TODO - Has to be in renderer, can cause issues because i cannot format cell data without it
        //      test- maybe i can use agGrid value-formatter? https://www.ag-grid.com/vue-data-grid/value-formatters/
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

            this.addRowToTheFirstPlace(newRow)

            if (this.lazyParams.size <= registryDescriptor.paginationLimit) {
                this.first = 0
            }
            this.$emit('rowChanged', newRow)

            console.log(this.rows)
        },
        cloneRows() {
            for (let i = this.selectedRows.length - 1; i >= 0; i--) {
                const tempRow = this.selectedRows[i]
                tempRow.uniqueId = cryptoRandomString({ length: 16, type: 'base64' })
                delete tempRow.id
                this.addRowToTheFirstPlace(tempRow)
            }
        },
        addRowToTheFirstPlace(newRow: any) {
            this.rows.unshift(newRow)
            // NOTE - applyTransaction alone wont add new row to this.rows, thats why we do both, to force table to refresh itself
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
                        if (this.selectedRow[el.field]) {
                            this.warningVisible = true
                        }
                    })
                } else {
                    this.clearDependentColumnsValues()
                }
            }

            row.edited = true
            this.$emit('rowChanged', row)
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
        },
        setDependentColumns(column: any) {
            let tempColumn = column

            if (!tempColumn.hasDependencies) {
                return
            }

            tempColumn.hasDependencies.forEach((el: any) => {
                this.dependentColumns.push(el)
                this.setDependentColumns(el)
            })
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
        showDefaultNumberFormatIcon(column: any) {
            if (!column || !column.columnInfo || !column.format) return false
            const inputType = setInputDataType(column.columnInfo.type)
            const temp = column.format.trim().match(numberFormatRegex)
            return inputType === 'number' && !temp
        },
        onSort(event: any) {
            this.multiSortMeta = event.multiSortMeta
        },
        onGridReady(params) {
            this.gridApi = params.api
            this.columnApi = params.columnApi

            this.refreshGridConfiguration()
        },
        refreshGridConfiguration() {
            this.gridApi.setColumnDefs(this.columns)
            this.gridApi.setRowData(this.rows)
            // this.gridApi.redrawRows()
        },

        cellWasClicked: (event) => {
            console.log('cell was clicked', event)
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
        onSelectionChanged() {
            this.selectedRows = this.gridApi.getSelectedRows()
        },
        pasteTest(ev) {
            const myCell = this.getFocusedCell(ev)

            if (ev.event.which === 17) {
                // 17 - ctrl
                this.ctrlDown = true
            } else if (ev.event.which == 67 && this.ctrlDown == true) {
                // 67 - c
                //TODO - copy styling here
                window.navigator.clipboard
                    .writeText(ev.value)
                    // .then(() => {
                    //     // myCell.cell.column.colDef.cellStyle = { border: '1px dashed #2196f3' }
                    //     ev.api.refreshCells({ force: true, columns: [myCell.column], rowNodes: [myCell.row] })
                    // })
                    .catch((er) => console.log(er))
            } else if (ev.event.which == 86 && this.ctrlDown == true) {
                // 67 - v
                window.navigator.clipboard.readText().then((value) => {
                    //TODO - paste validation here
                    // myCell.row.setDataValue(myCell.column, value)
                    this.setCellValue(myCell, value)
                })
            }
        },
        getFocusedCell(ev) {
            const focusedCell = ev.api.getFocusedCell()
            const rowNode = ev.api.getRowNode(focusedCell.rowIndex)
            const column = focusedCell.column.colDef.field
            return { cell: focusedCell, column: column, row: rowNode }
        },
        setCellValue(selectedCell, pasteValue) {
            var colDef = selectedCell.cell.column.colDef
            var cellType = this.getCellType(colDef)

            if (this.cellAcceptsPasteValue(colDef, cellType)) {
                switch (cellType) {
                    case 'text':
                        selectedCell.row.setDataValue(selectedCell.column, pasteValue)
                        break
                    case 'number':
                        console.log('IS NUMBER', pasteValue)
                        break
                    case 'dropdown':
                        //TODO  - dropdown valiodation, call BE service to see if pasted value is in the filtered array
                        if (!this.validateDropdownValueAfterCopyPaste(colDef, pasteValue)) {
                            this.setInfo({
                                //TODO - add cannot paste dropdon cell warning
                                title: 'Dropdown Warning',
                                msg: "Dropdown options doesn't contain pasted value!"
                            })
                        }
                        break
                    default:
                        break
                }
            }
        },
        validateDropdownValueAfterCopyPaste(colDef: any, pasteValue: string) {
            // console.log('%c Col Def ', 'background: #222; color: #bada55', colDef)
            // console.log('%c Col Def AB das edited 2', 'background: #222; color: #bada55', colDef.field)
            // console.log('%c pasteValue! ', 'background: #222; color: #bada55', pasteValue)
            // console.log('%c comboColumnOptions! ', 'background: #222; color: #bada55', this.comboColumnOptions)
            // console.log('%c comboColumnOptions field! ', 'background: #222; color: #bada55', this.comboColumnOptions[colDef.field]['All'])
            if (!this.comboColumnOptions[colDef.field] && !this.comboColumnOptions[colDef.field]['All']) return false
            const index = this.comboColumnOptions[colDef.field]['All'].findIndex((dropdownOption: any) => dropdownOption['column_1'] === pasteValue)
            // console.log('%c index ', 'background: #222; color: #bada55', index)
            return index !== -1
        },
        //TODO - ask if we want custom cell warnings for each case, or just a generic one
        cellAcceptsPasteValue(colDef, cellType) {
            if (colDef.editable == false || colDef.isEditable == false) {
                this.setInfo({
                    //TODO - add cannot paste non editable cell warning
                    title: 'NotEditable Warning',
                    msg: 'Cannot paste NotEditable values :)))))'
                })
                return false
            } else if (cellType === 'checkbox') {
                this.setInfo({
                    //TODO - add cannot paste checkbox cell warning
                    title: 'Checkbox Warning',
                    msg: 'Cannot paste checkbox values :)))))'
                })
                return false
            } else if (cellType === 'temporal') {
                this.setInfo({
                    //TODO - add cannot paste checkbox cell warning
                    title: 'Temporal Warning',
                    msg: 'Cannot paste temporal values :)))))'
                })
                return false
            } else return true
        },
        getCellType(colDef) {
            if (colDef.editorType == 'TEXT' && colDef.columnInfo.type === 'boolean') return 'checkbox'
            if (colDef.editorType !== 'COMBO' && colDef.columnInfo?.type !== 'date' && colDef.columnInfo?.type !== 'timestamp' && setInputDataType(colDef.columnInfo?.type) === 'text') return 'text'
            if (colDef.editorType !== 'COMBO' && colDef.columnInfo?.type !== 'date' && colDef.columnInfo?.type !== 'timestamp' && setInputDataType(colDef.columnInfo?.type) === 'number') return 'number'
            if (colDef.editorType === 'COMBO') return 'dropdown'
            if (colDef.columnInfo?.type === 'date' || colDef.columnInfo?.type === 'timestamp') return 'temporal'
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
.editableField {
    width: 100%;
}
.registry-grid-toolbar {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: end;
    height: 35px;
    border: 1px solid #babfc7;
    border-bottom: none;
}
</style>
