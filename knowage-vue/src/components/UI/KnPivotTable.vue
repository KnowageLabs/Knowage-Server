<template>
    {{ 'TODO Lazy Params: ' }}
    {{ lazyParams }}
    <table class="pivot-table" :style="descriptor.pivotStyles.table">
        <thead>
            <th v-for="(column, index) of columns.slice(1)" :key="index" :style="descriptor.pivotStyles.header">
                {{ column.field }}
                <i v-if="column.isEditable && column.type !== 'merge' && column.columnInfo.type !== 'boolean'" class="pi pi-pencil edit-icon p-ml-2" />
            </th>
            <th :style="descriptor.pivotStyles.iconColumn" />
        </thead>

        <tr v-for="(row, index) of mappedRows" :key="index">
            <template v-for="(column, i) of columns.slice(1)" :key="i">
                <td v-if="row[column.field].rowSpan > 0" :rowspan="row[column.field].rowSpan" :style="descriptor.pivotStyles.row">
                    <Checkbox v-if="column.editorType === 'TEXT' && column.columnInfo.type === 'boolean'" v-model="row[column.field].data" :binary="true" :disabled="!column.isEditable || column.type === 'merge'" @change="setRowEdited(row)"></Checkbox>
                    <InputText
                        :style="descriptor.pivotStyles.inputFields"
                        v-else-if="column.isEditable && column.type !== 'merge' && column.editorType !== 'COMBO' && column.columnInfo.type !== 'date'"
                        class="kn-material-input"
                        :type="setDataType(column.columnInfo.type)"
                        :step="getStep(column.columnInfo.type)"
                        v-model="row[column.field].data"
                        @input="setRowEdited(row)"
                    />
                    <Calendar
                        :style="descriptor.pivotStyles.inputFields"
                        style="height:20px"
                        class="pivot-calendar"
                        v-else-if="column.isEditable && column.type !== 'merge' && column.columnInfo.type === 'date'"
                        v-model="row[column.field].data"
                        :showTime="column.columnInfo.subtype === 'timestamp'"
                        :showSeconds="column.columnInfo.subtype === 'timestamp'"
                        :dateFormat="column.columnInfo.dateFormat"
                        :showButtonBar="true"
                        @date-select="setRowEdited(row)"
                    />
                    <Dropdown
                        class="kn-material-input"
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
                    <span v-else-if="!column.isEditable && row[column.field].data && (column.columnInfo.type === 'int' || column.columnInfo.type === 'float')">{{ getFormatedNumber(row[column.field].data) }}</span>
                    <span v-else>{{ row[column.field].data }}</span>
                </td>
            </template>
            <td><i v-if="row.edited" class="pi pi-flag" :style="descriptor.pivotStyles.iconColumn"></i></td>
        </tr>
    </table>

    <Paginator
        v-model:first="first"
        :rows="15"
        :totalRecords="lazyParams.size"
        :currentPageReportTemplate="
            $t('common.table.footer.paginated', {
                first: '{first}',
                last: '{last}',
                totalRecords: '{totalRecords}'
            })
        "
        paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
        @page="onPage($event)"
    ></Paginator>
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
import Paginator from 'primevue/paginator'
import RegistryDatatableWarningDialog from '@/modules/documentExecution/registry/tables/RegistryDatatableWarningDialog.vue'
import descriptor from '@/modules/documentExecution/registry/tables/RegistryDatatableDescriptor.json'

export default defineComponent({
    name: 'kn-pivot-table',
    components: { Calendar, Checkbox, Dropdown, Paginator, RegistryDatatableWarningDialog },
    props: {
        columns: [] as any,
        rows: [] as any,
        propConfiguration: { type: Object },
        entity: { type: String },
        id: { type: String },
        pagination: { type: Object }
    },
    emits: ['rowChanged', 'pageChanged'],
    created() {
        this.mapRows()
        this.checkForRowSpan(0, this.mappedRows.length - 1, this.mappedRows, this.columns, 1)
        this.loadPagination()
    },
    watch: {
        rows: {
            handler() {
                this.mapRows()
                this.checkForRowSpan(0, this.mappedRows.length - 1, this.mappedRows, this.columns, 1)
            },
            deep: true
        },
        pagination: {
            handler() {
                this.loadPagination()
                this.first = this.pagination?.start
            },
            deep: true
        }
    },
    data() {
        return {
            descriptor,
            mappedRows: [] as any,
            configuration: {} as any,
            comboColumnOptions: [] as any[],
            dependentColumns: [] as any[],
            selectedRow: null as any,
            warningVisible: false,
            stopWarnings: [] as any[],
            lazyParams: {} as any,
            first: 0
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
            // console.log('MAPPED ROWS: ', this.mappedRows)
            // console.log('COLUMNS: ', this.columns)
        },
        checkForRowSpan(fromIndex, toIndex, rows, columns, columnIndex) {
            const column = columns[columnIndex]

            if (column.type !== 'merge') {
                return
            }

            let groupCount = 1
            let startIndex = fromIndex
            for (let i = fromIndex + 1; i <= toIndex; i++) {
                if (rows[i - 1][column.field].data === rows[i][column.field].data) {
                    rows[i][column.field].rowSpan = 0
                    groupCount++
                }
                if (rows[i - 1][column.field].data !== rows[i][column.field].data || i === toIndex) {
                    rows[startIndex][column.field].rowSpan = groupCount
                    if (i - 1 > startIndex && columnIndex < columns.length - 1) {
                        this.checkForRowSpan(startIndex, i === toIndex ? i : i - 1, rows, columns, columnIndex + 1)
                    }
                    startIndex = i
                    groupCount = 1
                }
            }
        },
        loadPagination() {
            this.lazyParams = { ...this.pagination } as any
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
        setRowEdited(row: any) {
            row.edited = true
            this.$emit('rowChanged', row)
        },
        onDropdownChange(row: any, column: any) {
            row[column.field] = { data: row[column.field].data['column_1'], rowSpan: 1 }
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
            if (!this.comboColumnOptions[column.field]) {
                this.comboColumnOptions[column.field] = []
            }

            if (!this.comboColumnOptions[column.field][row[column.dependences]?.data]) {
                this.loadColumnOptions(column, row)
            }
        },
        async loadColumnOptions(column: any, row: any) {
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
            this.selectedRow.edited = true
            this.$emit('rowChanged', this.selectedRow)
        }
    }
})
</script>

<style scoped lang="scss">
.pivot-table table,
th,
td {
    border: 3px solid #5d8dbb93;
}
.p-component.p-inputtext {
    border: none !important;
}
.pivot-calendar {
    .p-inputtext {
        .p-component {
            border: none;
        }
    }
}
</style>
