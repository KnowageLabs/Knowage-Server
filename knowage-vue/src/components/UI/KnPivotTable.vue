<template>
    <table class="pivot-table">
        <thead>
            <th v-for="(column, index) of columns.slice(1)" :key="index">
                {{ column.field }}
            </th>
            <KnFabButton class="p-mb-5" icon="fas fa-plus" @click="addNewRow"></KnFabButton>
        </thead>
        <tr v-for="(row, index) of mappedRows" :key="index">
            <template v-for="(column, i) of columns.slice(1)" :key="i">
                <td v-if="row[column.field].rowSpan > 0" :rowspan="row[column.field].rowSpan">
                    <!-- <span>{{ row[column.field].data }}</span>
                    <span>{{ row[column.field] }}</span> -->
                    <!-- <span>{{ row[column.field].data }}</span> -->
                    <Checkbox v-if="column.editorType === 'TEXT' && column.columnInfo.type === 'boolean'" v-model="row[column.field].data" :binary="true" :disabled="!column.isEditable || column.type === 'merge'" @change="$emit('rowChanged', row)"></Checkbox>
                    <InputText
                        v-else-if="column.isEditable && column.type !== 'merge' && column.editorType !== 'COMBO' && column.columnInfo.type !== 'date'"
                        class="p-inputtext-sm"
                        :type="setDataType(column.columnInfo.type)"
                        :step="getStep(column.columnInfo.type)"
                        v-model="row[column.field].data"
                        @input="$emit('rowChanged', row)"
                    />
                    <Calendar
                        v-else-if="column.isEditable && column.type !== 'merge' && column.columnInfo.type === 'date'"
                        v-model="row[column.field].data"
                        :showTime="column.columnInfo.subtype === 'timestamp'"
                        :showSeconds="column.columnInfo.subtype === 'timestamp'"
                        :dateFormat="column.columnInfo.dateFormat"
                        @date-select="$emit('rowChanged', row)"
                    />
                    <Dropdown
                        v-else-if="column.isEditable && column.editorType === 'COMBO'"
                        v-model="row[column.field].data"
                        :options="comboColumnOptions[column.field] ? comboColumnOptions[column.field][row[column.dependences]?.data] : []"
                        :placeholder="$t('documentExecution.registry.select')"
                        @change="onDropdownChange(row, column)"
                        @before-show="addColumnOptions(column, row)"
                    >
                        <template #value="slotProps">
                            <div v-if="slotProps.value">
                                <span>{{ slotProps.value }}</span>
                            </div>
                        </template>
                        <template #option="slotProps">
                            <div>
                                <span>{{ slotProps.option['column_1'] }}</span>
                            </div>
                        </template>
                    </Dropdown>
                    <span v-else-if="!column.isEditable && column.columnInfo.type === 'date'">{{ getFormatedDate(row[column.field].data, column.columnInfo.dateFormat) }} </span>
                    <span v-else-if="(!column.isEditable && column.columnInfo.type === 'int') || column.columnInfo.type === 'float'">{{ getFormatedNumber(row[column.field].data) }}</span>
                    <span v-else>{{ row[column.field].data }}</span>
                    <i v-if="column.isEditable && column.type === 'merge' && column.columnInfo.type !== 'boolean'" class="pi pi-pencil edit-icon p-ml-2" />
                </td>
            </template>
            <td><Button icon="pi pi-trash" class="p-button-link" @click="rowDeleteConfirm(index, row)" /></td>
        </tr>
    </table>

    <RegistryDatatableWarningDialog :visible="warningVisible" :columns="dependentColumns" @close="onWarningDialogClose"></RegistryDatatableWarningDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { setInputDataType, getInputStep } from '@/helpers/commons/tableHelpers'
import { formatDateWithLocale, formatNumberWithLocale } from '@/helpers/commons/localeHelper'
import axios from 'axios'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import RegistryDatatableWarningDialog from '@/modules/documentExecution/registry/tables/RegistryDatatableWarningDialog.vue'

export default defineComponent({
    name: 'kn-pivot-table',
    components: { Calendar, Checkbox, Dropdown, KnFabButton, RegistryDatatableWarningDialog },
    props: {
        columns: [] as any,
        rows: [] as any,
        propConfiguration: { type: Object },
        entity: { type: String },
        id: { type: String }
    },
    emits: ['rowChanged', 'rowDeleted'],
    created() {
        this.mapRows()
        this.checkForRowSpan(0, this.mappedRows.length - 1, this.mappedRows, this.columns, 1)
    },
    watch: {
        rows: {
            handler() {
                this.mapRows()
                this.checkForRowSpan(0, this.mappedRows.length - 1, this.mappedRows, this.columns, 1)
            },
            deep: true
        }
    },
    data() {
        return {
            mappedRows: [] as any,
            configuration: {} as any,
            buttons: {
                enableButtons: false,
                enableDeleteRecords: false,
                enableAddRecords: false
            },
            comboColumnOptions: [] as any[],
            dependentColumns: [] as any[],
            selectedRow: null as any,
            warningVisible: false,
            stopWarnings: [] as any[]
        }
    },
    computed: {},
    methods: {
        mapRows() {
            this.mappedRows = this.rows.map((row) => {
                let newRow = { id: row.id }
                this.columns.forEach((column) => {
                    newRow[column.field] = { data: row[column.field], rowSpan: 1 }
                })
                return newRow
            })
            console.log('MAPPED ROWS: ', this.mappedRows)
            console.log('COLUMNS: ', this.columns)
        },
        checkForRowSpan(fromIndex, toIndex, rows, columns, columnIndex) {
            const column = columns[columnIndex]

            if (!column.grouping) {
                return
            }

            // console.log(fromIndex, toIndex, column)
            // console.log('LINE 61 fromIndex: ', fromIndex, ', toIndex: ', toIndex, ', rows: ', rows, ', columns: ', columns, ', columnIndex: ', columnIndex, ', column: ', column)
            let groupCount = 1
            let startIndex = fromIndex
            for (let i = fromIndex + 1; i <= toIndex; i++) {
                // console.log('i', i)
                // console.log(rows[i - 1][column.field].data, '===', rows[i][column.field].data)
                // console.log('LINE 70 i: ', i, ', comparing: ', rows[i - 1][column.field].data, ' === ', rows[i][column.field].data)
                if (rows[i - 1][column.field].data === rows[i][column.field].data) {
                    rows[i][column.field].rowSpan = 0
                    groupCount++
                }
                if (rows[i - 1][column.field].data !== rows[i][column.field].data || i === toIndex) {
                    // console.log('groupCount', column.field, rows[startIndex][column.field].data, groupCount)
                    // console.log('LINE 77 columnField: ', column.field, ', rows[startIndex][column.field].data: ', rows[startIndex][column.field].data, ', groupCount: ', groupCount)
                    rows[startIndex][column.field].rowSpan = groupCount
                    if (i - 1 > startIndex && columnIndex < columns.length - 1) {
                        // console.log('LINE 82: Before recursive call')
                        this.checkForRowSpan(startIndex, i === toIndex ? i : i - 1, rows, columns, columnIndex + 1)
                    }
                    startIndex = i
                    groupCount = 1
                }
            }
        },
        setDataType(columnType: string) {
            return setInputDataType(columnType)
        },
        getStep(dataType: string) {
            return getInputStep(dataType)
        },
        getFormatedDate(date: any, format: any) {
            return formatDateWithLocale(date, format)
        },
        getFormatedNumber(number: number, precision?: number, format?: any) {
            return formatNumberWithLocale(number, precision, format)
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
            console.log('LOADED CONFIGURATION: ', this.configuration)
            console.log('LOADED BUTONS: ', this.buttons)
        },
        addNewRow() {
            const newRow = { id: this.rows.length, isNew: true }
            this.columns.forEach((el: any) => {
                if (el.isVisible && el.field !== 'id') {
                    const data = el.defaultValue ?? ''
                    newRow[el.field] = { data: data, rowSpan: 1 }
                }
            })
            this.mappedRows.unshift(newRow)

            // if (this.lazyParams.size <= registryDatatableDescriptor.tableOptions.paginationLimit) {
            //     this.first = 0
            // }

            this.$emit('rowChanged', newRow)
        },
        rowDeleteConfirm(index: number, row: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteRow(index, row)
            })
        },
        deleteRow(index: number, row: any) {
            console.log('INDEX FOR DELETE: ', index)
            console.log('ROW FOR DELETE: ', row)
            row.isNew ? this.mappedRows.splice(index, 1) : this.$emit('rowDeleted', row)
        },
        onDropdownChange(row: any, column: any) {
            row[column.field] = { data: row[column.field].data['column_1'], rowSpan: 1 }
            this.selectedRow = row
            if (column.hasDependencies && !this.stopWarnings[column.field]) {
                this.dependentColumns = [] as any[]
                this.setDependentColumns(column)

                this.dependentColumns.forEach((el: any) => {
                    if (this.selectedRow[el.field]) {
                        this.warningVisible = true
                    }
                })
            } else {
                this.clearDependentColumnsValues()
            }

            this.$emit('rowChanged', row)
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
        addColumnOptions(column: any, row: any) {
            console.log('BLA BEFORE: ', row[column.field].data)
            if (!this.comboColumnOptions[column.field]) {
                this.comboColumnOptions[column.field] = []
            }

            if (!this.comboColumnOptions[column.field][row[column.dependences]?.data]) {
                this.loadColumnOptions(column, row)
            }
            console.log('BLA AFTER: ', row[column.field].data)
        },
        async loadColumnOptions(column: any, row: any) {
            console.log('ROW FOR LOAD COLUMN OPTIONS BEFORE: ', row)
            console.log('Column dependences BEFORE: ', column.dependences)
            console.log('TEEEEEEEEEST BEFORE: ', this.comboColumnOptions[column.field][row[column.dependences]])

            const subEntity = column.subEntity ? '::' + column.subEntity + '(' + column.foreignKey + ')' : ''

            const entityId = this.entity + subEntity + ':' + column.field
            const entityOrder = this.entity + subEntity + ':' + (column.orderBy ?? column.field)

            const postData = new URLSearchParams({ ENTITY_ID: entityId, QUERY_TYPE: 'standard', ORDER_ENTITY: entityOrder, ORDER_TYPE: 'asc', QUERY_ROOT_ENTITY: 'true' })
            if (column.dependences && row && row[column.dependences].data) {
                postData.append('DEPENDENCES', this.entity + subEntity + ':' + column.dependences + '=' + row[column.dependences].data)
            }
            await axios
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then((response) => (this.comboColumnOptions[column.field][row[column.dependences]?.data] = response.data.rows))

            console.log('ROW FOR LOAD COLUMN OPTIONS: ', row)
            console.log('Column dependences: ', column.dependences)
            console.log('TEEEEEEEEEST: ', this.comboColumnOptions[column.field][row[column.dependences]])
        },
        onWarningDialogClose(payload: any) {
            if (payload.stopWarnings) {
                this.stopWarnings[payload.columnField] = true
            }

            this.clearDependentColumnsValues()
            this.warningVisible = false
        },
        clearDependentColumnsValues() {
            this.dependentColumns.forEach((el: any) => (this.selectedRow[el.field] = { data: '', rowSpan: 1 }))
            this.$emit('rowChanged', this.selectedRow)
        }
    }
})
</script>

<style scoped lang="scss">
.pivot-table table,
th,
td {
    border: 1px solid black;
}
</style>
