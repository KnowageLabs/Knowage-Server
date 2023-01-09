<template>
    <Checkbox v-if="params.colDef.editorType == 'TEXT' && params.colDef.columnInfo.type === 'boolean'" :disabled="!params.colDef.isEditable" v-model="value" :binary="true" @change=""></Checkbox>
    <InputText
        v-if="column && column.editorType !== 'COMBO' && column.columnInfo?.type !== 'date' && column.columnInfo?.type !== 'timestamp' && getDataType(column.columnInfo?.type) === 'text'"
        class="kn-material-input"
        :type="'text'"
        :step="getStep(column.columnInfo?.type)"
        v-model="value"
        @input="onRowChanged(row)"
    />
    <InputNumber
        v-if="column && column.editorType !== 'COMBO' && column.columnInfo?.type !== 'date' && column.columnInfo?.type !== 'timestamp' && getDataType(column.columnInfo?.type) === 'number'"
        class="kn-material-input p-inputtext-sm"
        v-model="value"
        :useGrouping="useGrouping"
        :locale="locale"
        :minFractionDigits="minFractionDigits"
        :maxFractionDigits="maxFractionDigits"
        :disabled="!column.isEditable"
        @blur="onInputNumberChange"
    >
    </InputNumber>
    <Dropdown
        v-else-if="column && column.editorType === 'COMBO'"
        class="kn-material-input"
        v-model="value"
        :options="getOptions(column, row)"
        optionValue="column_1"
        optionLabel="column_1"
        @change="onDropdownChange({ row: row, column: column })"
        @before-show="addColumnOptions({ row: row, column: column })"
        :filter="true"
    >
    </Dropdown>
    <Calendar
        v-else-if="column && (column.columnInfo?.type === 'date' || column.columnInfo?.type === 'timestamp')"
        class="pivot-calendar"
        :style="registryDatatableDescriptor.pivotStyles.inputFields"
        v-model="value"
        :showTime="column.columnInfo?.type === 'timestamp'"
        :showSeconds="column.columnInfo?.type === 'timestamp'"
        :showButtonBar="true"
        @date-select="onRowChanged(row)"
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
import Checkbox from 'primevue/checkbox'
import registryDatatableDescriptor from '../RegistryDatatableDescriptor.json'

export default defineComponent({
    name: 'registry-datatable-editable-field',
    components: { Calendar, Dropdown, InputNumber, Checkbox },
    props: {
        // column: { type: Object },
        // propRow: { type: Object },
        // comboColumnOptions: { type: Array },
        params: {
            required: true,
            type: Object as any
        }
    },
    emits: ['rowChanged', 'dropdownChanged', 'dropdownOpened'],
    data() {
        return {
            registryDatatableDescriptor,
            row: {} as any,
            column: {} as any,
            columnOptions: [] as any[],
            options: [] as any[],
            useGrouping: false,
            locale: '',
            minFractionDigits: 2,
            maxFractionDigits: 2,
            value: null as any
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
        this.column = this.params.colDef
        this.loadColumnOptions()
        this.value = this.getInitialValue()
    },
    computed: {
        getCurrentLocaleDefaultDateFormat() {
            return (column) => column.format || primeVueDate()
        }
    },
    methods: {
        loadRow() {
            this.row = this.params.data
            if (this.column && (this.row[this.column.field] || this.row[this.column.field] === 0 || this.row[this.column.field] === '')) {
                if (this.column.columnInfo?.type === 'date' && typeof this.row[this.column.field] === 'string') {
                    this.row[this.column.field] = this.row[this.column.field] ? new Date(luxonFormatDate(this.row[this.column.field], 'yyyy-MM-dd', 'yyyy-MM-dd')) : null
                } else if (this.column.columnInfo?.type === 'timestamp' && typeof this.row[this.column.field] === 'string' && this.row[this.column.field] !== '') {
                    this.row[this.column.field] = new Date(luxonFormatDate(this.row[this.column.field], 'yyyy-MM-dd HH:mm:ss.S', 'yyyy-MM-dd HH:mm:ss.S'))
                } else if (this.column.editorType !== 'COMBO' && this.column.columnInfo?.type !== 'date' && this.column.columnInfo?.type !== 'timestamp' && this.getDataType(this.column.columnInfo?.type) === 'number') {
                    this.formatNumberConfiguration()
                }
            }
        },
        formatNumberConfiguration() {
            if (this.column?.columnInfo?.type === 'int') {
                this.useGrouping = false
                this.minFractionDigits = 0
                this.maxFractionDigits = 0
                return
            }
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
            this.columnOptions = this.params.comboColumnOptions as any[]
        },
        getFormattedDate(date: any, format: any, incomingFormat?: string) {
            return formatDate(date, format, incomingFormat)
        },
        onInputNumberChange() {
            setTimeout(() => this.onRowChanged(this.row), 250)
        },
        getOptions(column: any, row: any) {
            let options = this.columnOptions && this.columnOptions[column.field] ? this.columnOptions[column.field][row[column.dependences]] : []
            if (!options || options.length === 0) options = this.columnOptions[column.field]['All']
            return options ?? []
        },
        getValue() {
            return this.value
        },
        getInitialValue() {
            let startValue = this.params.value
            const isBackspaceOrDelete = this.params.eventKey === 'Backspace' || this.params.eventKey === 'Delete'
            if (isBackspaceOrDelete) startValue = null
            if (startValue !== null && startValue !== undefined) return startValue
            return null
        },
        onRowChanged(payload: any) {
            console.log('onRowChanged', payload)
            this.params.context.componentParent.setRowEdited(payload)
            // this.value = payload.row[this.params.colDef.field]
        },
        onDropdownChange(payload: any) {
            console.log('onDropdownChange', payload)
            this.params.context.componentParent.onDropdownChange(payload)
            // this.value = payload.row[this.params.colDef.field]
        },
        addColumnOptions(payload: any) {
            console.log('addColumnOptions', payload)
            this.params.context.componentParent.addColumnOptions(payload)
            // this.value = payload.row[this.params.colDef.field]
        }
    }
})
</script>

<style scoped lang="scss">
.p-component {
    &.pivot-calendar,
    &.p-inputtext,
    &.p-dropdown,
    &.p-datepicker {
        border: none !important;
        background-color: transparent !important;
        width: 100% !important;
    }
}

.p-inputnumber,
.p-calendar {
    &:deep(.p-inputtext) {
        border: none !important;
        background-color: transparent !important;
        width: 100% !important;
    }
}
</style>
