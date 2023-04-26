<template>
    <div v-if="column">
        <InputText
            v-if="column.editorType !== 'COMBO' && column.columnInfo.type !== 'date' && column.columnInfo.type !== 'timestamp'"
            v-model="row[column.field].data"
            :style="knPivotTableDescriptor.pivotStyles.inputFields"
            class="kn-material-input"
            :type="setDataType(column.columnInfo.type)"
            :step="getStep(column.columnInfo.type)"
            @input="$emit('rowChanged', row)"
        />
        <Calendar
            v-else-if="column.columnInfo.type === 'date' || column.columnInfo.type === 'timestamp'"
            v-model="row[column.field].data"
            :style="knPivotTableDescriptor.pivotStyles.inputFields"
            class="pivot-calendar"
            :show-time="column.columnInfo.type === 'timestamp'"
            :show-seconds="column.columnInfo.type === 'timestamp'"
            :show-button-bar="true"
            :date-format="column.columnInfo.type === 'date' ? getCurrentLocaleDefaultDateFormat(column) : ''"
            @date-select="$emit('rowChanged', row)"
        />
        <Dropdown
            v-else-if="column.editorType === 'COMBO'"
            v-model="row[column.field].data"
            class="kn-material-input"
            :options="columnOptions[column.field] ? columnOptions[column.field][row[column.dependences]?.data] : []"
            :placeholder="$t('documentExecution.registry.select')"
            @change="$emit('dropdownChanged', { row: row, column: column })"
            @before-show="$emit('dropdownOpened', { row: row, column: column })"
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
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { setInputDataType, getInputStep } from '@/helpers/commons/tableHelpers'
import { formatDate } from '@/helpers/commons/localeHelper'
import { luxonFormatDate, primeVueDate } from '@/helpers/commons/localeHelper'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import knPivotTableDescriptor from '@/components/UI/KnPivotTable/KnPivotTableDescriptor.json'

export default defineComponent({
    name: 'kn-pivot-table-editable-field',
    components: { Calendar, Dropdown },
    props: { column: { type: Object }, propRow: { type: Object }, comboColumnOptions: { type: Array } },
    emits: ['rowChanged', 'dropdownChanged', 'dropdownOpened'],
    data() {
        return {
            knPivotTableDescriptor,
            row: {} as any,
            columnOptions: [] as any[]
        }
    },
    computed: {
        getCurrentLocaleDefaultDateFormat() {
            return (column) => column.format || primeVueDate()
        }
    },
    watch: {
        propRow() {
            this.loadRow()
        },
        comboColumnOptions: {
            handler() {
                this.loadColumnOptions()
            },
            deep: true
        }
    },
    created() {
        this.loadRow()
        this.loadColumnOptions()
    },
    methods: {
        loadRow() {
            this.row = this.propRow
            const data = this.row[this.column?.field]?.data
            if (data && typeof data === 'string') {
                if (this.column?.columnInfo.type === 'date') {
                    this.row[this.column.field].data = new Date(luxonFormatDate(data, 'yyyy-MM-dd', 'yyyy-MM-dd'))
                } else if (this.column?.columnInfo.type === 'timestamp' && this.row[this.column.field] !== '') {
                    this.row[this.column.field].data = new Date(luxonFormatDate(data, 'yyyy-MM-dd HH:mm:ss.S', 'yyyy-MM-dd HH:mm:ss.S'))
                }
            }
        },
        setDataType(columnType: string) {
            return setInputDataType(columnType)
        },
        getStep(dataType: string) {
            return getInputStep(dataType)
        },
        loadColumnOptions() {
            this.columnOptions = this.comboColumnOptions as any[]
        },
        getFormattedDate(date: any, format: any) {
            return formatDate(date, format)
        }
    }
})
</script>

<style scoped>
.pivot-calendar .p-inputtext {
    border: none;
    background-color: transparent;
}
</style>
