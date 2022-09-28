<template>
    <InputText
        class="kn-material-input"
        v-if="column && column.editorType !== 'COMBO' && column.columnInfo?.type !== 'date' && column.columnInfo?.type !== 'timestamp' && getDataType(column.columnInfo.type) === 'text'"
        :type="'text'"
        :step="getStep(column.columnInfo.type)"
        v-model="row[column.field]"
        @input="$emit('rowChanged', row)"
    />
    <InputNumber
        class="kn-material-input p-inputtext-sm"
        v-if="column && column.editorType !== 'COMBO' && column.columnInfo?.type !== 'date' && column.columnInfo?.type !== 'timestamp' && getDataType(column.columnInfo.type) === 'number'"
        v-model="row[column.field]"
        :useGrouping="useGrouping"
        :locale="locale"
        :minFractionDigits="minFractionDigits"
        :maxFractionDigits="maxFractionDigits"
        :disabled="!column.isEditable"
        @input="$emit('rowChanged', row)"
    >
    </InputNumber>
    <Dropdown
        class="kn-material-input"
        v-else-if="column && column.editorType === 'COMBO'"
        v-model="row[column.field]"
        :options="columnOptions && columnOptions[column.field] ? columnOptions[column.field][row[column.dependences]] : []"
        optionValue="column_1"
        optionLabel="column_1"
        @change="$emit('dropdownChanged', { row: row, column: column })"
        @before-show="$emit('dropdownOpened', { row: row, column: column })"
        :filter="true"
    >
    </Dropdown>
    <Calendar
        :style="registryDatatableDescriptor.pivotStyles.inputFields"
        class="pivot-calendar"
        v-else-if="column && (column.columnInfo?.type === 'date' || column.columnInfo?.type === 'timestamp')"
        v-model="row[column.field]"
        :showTime="column.columnInfo?.type === 'timestamp'"
        :showSeconds="column.columnInfo?.type === 'timestamp'"
        :showButtonBar="true"
        @date-select="$emit('rowChanged', row)"
        :dateFormat="column.columnInfo?.type === 'date' ? getCurrentLocaleDefaultDateFormat(column) : ''"
    />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { setInputDataType, getInputStep, formatNumber } from '@/helpers/commons/tableHelpers'
import { formatDate, getLocale } from '@/helpers/commons/localeHelper'
import { luxonFormatDate, primeVueDate } from '@/helpers/commons/localeHelper'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import registryDatatableDescriptor from './RegistryDatatableDescriptor.json'

export default defineComponent({
    name: 'registry-datatable-editable-field',
    components: { Calendar, Dropdown, InputNumber },
    props: { column: { type: Object }, propRow: { type: Object }, comboColumnOptions: { type: Array } },
    emits: ['rowChanged', 'dropdownChanged', 'dropdownOpened'],
    data() {
        return {
            registryDatatableDescriptor,
            row: {} as any,
            columnOptions: [] as any[],
            options: [] as any[],
            useGrouping: false,
            locale: '',
            minFractionDigits: 2,
            maxFractionDigits: 2
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
        this.setDefaultLocale()
        this.loadRow()
        this.loadColumnOptions()
    },
    computed: {
        getCurrentLocaleDefaultDateFormat() {
            return (column) => column.format || primeVueDate()
        }
    },
    methods: {
        loadRow() {
            this.row = this.propRow
            if (this.column && (this.row[this.column.field] || this.row[this.column.field] === 0 || this.row[this.column.field] === '')) {
                if (this.column.columnInfo.type === 'date' && typeof this.row[this.column.field] === 'string') {
                    this.row[this.column.field] = new Date(luxonFormatDate(this.row[this.column.field], 'yyyy-MM-dd', 'yyyy-MM-dd'))
                } else if (this.column.columnInfo.type === 'timestamp' && typeof this.row[this.column.field] === 'string') {
                    this.row[this.column.field] = new Date(luxonFormatDate(this.row[this.column.field], 'yyyy-MM-dd HH:mm:ss.S', 'yyyy-MM-dd HH:mm:ss.S'))
                } else if (this.column.editorType !== 'COMBO' && this.column.columnInfo.type !== 'date' && this.column.columnInfo.type !== 'timestamp' && this.getDataType(this.column.columnInfo.type) === 'number') {
                    this.formatNumberConfiguration()
                }
            }
        },
        formatNumberConfiguration() {
            const configuration = formatNumber(this.column)
            if (configuration) {
                this.useGrouping = configuration.useGrouping
                this.minFractionDigits = configuration.minFractionDigits
                this.maxFractionDigits = configuration.maxFractionDigits
            }
        },
        setDefaultLocale() {
            const locale = getLocale()
            this.locale = locale ? locale.replace('_', '-') : ''
        },
        getDataType(columnType: string) {
            return setInputDataType(columnType)
        },
        getStep(dataType: string) {
            return getInputStep(dataType)
        },
        loadColumnOptions() {
            this.columnOptions = this.comboColumnOptions as any[]
        },
        getFormattedDate(date: any, format: any, incomingFormat?: string) {
            return formatDate(date, format, incomingFormat)
        }
    }
})
</script>

<style scoped lang="scss">
.pivot-calendar.p-component,
.p-inputtext.p-component,
.p-dropdown.p-component {
    border: none !important;
    background-color: transparent !important;
    width: 100% !important;
}
</style>
