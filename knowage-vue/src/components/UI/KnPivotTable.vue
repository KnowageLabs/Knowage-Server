<template>
    <table class="pivot-table">
        <thead>
            <th v-for="(column, index) of columns" :key="index">
                {{ column.field }}
            </th>
        </thead>
        <tr v-for="(row, index) of mappedRows" :key="index">
            <template v-for="(column, i) of columns" :key="i">
                <td v-if="row[column.field].rowSpan > 0" :rowspan="row[column.field].rowSpan">
                    <!-- <span>{{ row[column.field].data }} -- {{ row[column.field].rowSpan }}</span> -->
                    <Checkbox v-if="column.editorType === 'TEXT' && column.columnInfo.type === 'boolean'" v-model="row[column.field].data" :binary="true" :disabled="!column.isEditable" @change="$emit('rowChanged', row)"></Checkbox>
                    <InputText
                        v-else-if="column.isEditable && column.editorType !== 'COMBO' && column.columnInfo.type !== 'date'"
                        class="p-inputtext-sm"
                        :type="setDataType(column.columnInfo.type)"
                        :step="getStep(column.columnInfo.type)"
                        v-model="row[column.field].data"
                        @input="$emit('rowChanged', row)"
                    />
                    <Calendar
                        v-else-if="column.isEditable && column.columnInfo.type === 'date'"
                        v-model="row[column.field].data"
                        :showTime="column.columnInfo.subtype === 'timestamp'"
                        :showSeconds="column.columnInfo.subtype === 'timestamp'"
                        :dateFormat="column.columnInfo.dateFormat"
                        @date-select="$emit('rowChanged', row)"
                    />
                    <span v-else-if="!column.isEditable && column.columnInfo.type === 'date'">{{ getFormatedDate(row[column.field].data, column.columnInfo.dateFormat) }} </span>
                    <span v-else-if="(!column.isEditable && column.columnInfo.type === 'int') || column.columnInfo.type === 'float'">{{ getFormatedNumber(row[column.field].data) }}</span>
                    <span v-else>{{ row[column.field].data }}</span>
                    <i v-if="column.isEditable && column.columnInfo.type !== 'boolean'" class="pi pi-pencil edit-icon p-ml-2" />
                </td>
            </template>
        </tr>
    </table>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { setInputDataType, getInputStep } from '@/helpers/commons/tableHelpers'
import { formatDateWithLocale, formatNumberWithLocale } from '@/helpers/commons/localeHelper'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'

// TODO Dropdown

export default defineComponent({
    name: 'kn-pivot-table',
    components: { Calendar, Checkbox },
    props: {
        columns: [] as any,
        rows: [] as any
    },
    emits: ['rowChanged'],
    created() {
        this.mapRows()
        this.checkForRowSpan(0, this.mappedRows.length - 1, this.mappedRows, this.columns, 0)
    },
    watch: {
        rows() {
            this.mapRows()
            this.checkForRowSpan(0, this.mappedRows.length - 1, this.mappedRows, this.columns, 0)
        }
    },
    data() {
        return {
            mappedRows: [] as any
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

            console.log(fromIndex, toIndex, column)
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
