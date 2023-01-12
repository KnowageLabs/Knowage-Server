<template>
    <div class="kn-height-full p-d-flex p-flex-row p-ai-center">
        <Checkbox v-if="getCellType(column) === 'checkbox'" v-model="value" class="p-ml-2" :binary="true" ref="input" />
        <Textarea v-if="getCellType(column) === 'text'" class="kn-material-input kn-width-full" rows="4" v-model="value" :step="getStep(column.columnInfo?.type)" maxlength="250" @input="onRowChanged(row)" ref="input" />
        <InputNumber
            v-if="getCellType(column) === 'number'"
            class="kn-material-input p-inputtext-sm kn-width-full kn-height-full"
            v-model="value"
            :useGrouping="useGrouping"
            :locale="locale"
            :minFractionDigits="minFractionDigits"
            :maxFractionDigits="maxFractionDigits"
            :disabled="!column.isEditable"
            @blur="onInputNumberChange"
            ref="input"
        />
        <Dropdown
            v-else-if="getCellType(column) === 'dropdown'"
            class="kn-material-input kn-width-full"
            v-model="value"
            :options="getOptions(column, row)"
            optionValue="column_1"
            optionLabel="column_1"
            @change="onDropdownChange({ row: row, column: column })"
            @before-show="addColumnOptions({ row: row, column: column })"
            :filter="true"
            ref="input"
        />
        <Calendar
            v-else-if="getCellType(column) === 'temporal'"
            class="registry-no-borders kn-width-full kn-height-full"
            :style="registryDatatableDescriptor.pivotStyles.inputFields"
            v-model="value"
            :showTime="column.columnInfo?.type === 'timestamp'"
            :showSeconds="column.columnInfo?.type === 'timestamp'"
            :showButtonBar="true"
            @date-select="onRowChanged(row)"
            :dateFormat="column.columnInfo?.type === 'date' ? getCurrentLocaleDefaultDateFormat(column) : ''"
            ref="input"
        />
    </div>
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
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'registry-datatable-editable-field',
    components: { Calendar, Dropdown, InputNumber, Checkbox, Textarea },
    props: {
        comboColumnOptions: { type: Array },
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
    mounted() {
        this.$nextTick(() => {
            const inputFocus = this.$refs['input'] as any
            switch (this.getCellType(this.params.colDef)) {
                case 'text':
                    return inputFocus.$el.focus()
                case 'checkbox':
                case 'dropdown':
                case 'temporal':
                case 'number':
                    return inputFocus.$el.children[0].focus()
                default:
                    return inputFocus.$el.focus()
            }
        })
    },
    created() {
        this.setDefaultLocale()
        this.column = this.params.colDef
        this.loadRow()
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
            setTimeout(() => {
                this.row[this.column.field] = this.value
                this.onRowChanged(this.row)
            }, 250)
        },
        getOptions(column: any, row: any) {
            let options = this.columnOptions && this.columnOptions[column.field] ? this.columnOptions[column.field][row[column.dependences]] : []
            if (!options || options.length === 0) options = this.columnOptions[column.field]['All']
            return options ?? []
        },
        getValue() {
            return this.value
        },
        isPopup() {
            switch (this.getCellType(this.params.colDef)) {
                case 'text':
                    return true
                default:
                    return false
            }
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
        },
        onDropdownChange(payload: any) {
            console.log('onDropdownChange', payload)
            this.params.context.componentParent.onDropdownChange(payload)
        },
        addColumnOptions(payload: any) {
            console.log('addColumnOptions', payload)
            this.params.context.componentParent.addColumnOptions(payload)
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

<style lang="scss"></style>
