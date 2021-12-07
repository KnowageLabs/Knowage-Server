<template>
    <InputText class="kn-material-input" v-if="column.editorType !== 'COMBO' && column.columnInfo.type !== 'date'" :type="setDataType(column.columnInfo.type)" :step="getStep(column.columnInfo.type)" v-model="row[column.field]" @input="$emit('rowChanged', row)" />
    <Dropdown
        class="kn-material-input"
        v-else-if="column.editorType === 'COMBO'"
        v-model="row[column.field]"
        :options="columnOptions && columnOptions[column.field] ? columnOptions[column.field][row[column.dependences]] : []"
        optionValue="column_1"
        optionLabel="column_1"
        @change="$emit('dropdownChanged', { row: row, column: column })"
        @before-show="$emit('dropdownOpened', { row: row, column: column })"
        :filter="true"
    >
    </Dropdown>
    <!-- Calendar -->
    <Calendar
        :style="registryDatatableDescriptor.pivotStyles.inputFields"
        class="pivot-calendar"
        v-else-if="column.columnInfo.type === 'date'"
        v-model="row[column.field]"
        :showTime="column.columnInfo.subtype === 'timestamp'"
        :showSeconds="column.columnInfo.subtype === 'timestamp'"
        :showButtonBar="true"
        @date-select="$emit('rowChanged', row)"
    />
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { setInputDataType, getInputStep } from '@/helpers/commons/tableHelpers'
    import { formatDate } from '@/helpers/commons/localeHelper'
    import Calendar from 'primevue/calendar'
    import Dropdown from 'primevue/dropdown'
    import registryDatatableDescriptor from './RegistryDatatableDescriptor.json'

    export default defineComponent({
        name: 'registry-datatable-editable-field',
        components: { Calendar, Dropdown },
        props: { column: { type: Object }, propRow: { type: Object }, comboColumnOptions: { type: Array } },
        emits: ['rowChanged', 'dropdownChanged', 'dropdownOpened'],
        data() {
            return {
                registryDatatableDescriptor,
                row: {} as any,
                columnOptions: [] as any[],
                options: [] as any[]
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
                if (this.column?.columnInfo.type === 'date' && this.row[this.column.field]) {
                    this.row[this.column.field] = this.getFormattedDate(this.row[this.column.field], 'MM/DD/YYYY HH:mm:ss')
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

<style>
    .pivot-calendar .p-inputtext {
        border: none;
    }
</style>
