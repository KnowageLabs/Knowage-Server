<template>
    <DataTable class="p-datatable-sm kn-table" :value="rows" editMode="cell" dataKey="id" responsiveLayout="stack" breakpoint="960px">
        <template v-for="col of columns" :key="col.field">
            <Column class="kn-truncated" :field="col.field" :header="col.title">
                <template #editor="slotProps">
                    <span v-if="!col.editable && col.columnInfo.type !== 'boolean'">{{ slotProps.data[col.field] }}</span>
                    <!-- Checkbox -->
                    <Checkbox v-else-if="col.editor === 'TEXT' && col.columnInfo.type === 'boolean'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @click="$emit('rowChanged', slotProps.data)" :disabled="!col.editable"></Checkbox>
                    <InputText
                        v-else-if="col.editor !== 'COMBO' && col.editable && col.columnInfo.type !== 'date'"
                        class="p-inputtext-sm"
                        :type="setDataType(col.columnInfo.type)"
                        :step="getStep(col.columnInfo.type)"
                        v-model="slotProps.data[slotProps.column.props.field]"
                        @input="$emit('rowChanged', slotProps.data)"
                    />
                    <!-- Dropdown -->
                    <Dropdown v-else-if="col.editor === 'COMBO'" v-model="slotProps.data[col.field]" :options="this.comboColumnOptions[col.field]" optionValue="column_1" optionLabel="column_1" @change="$emit('rowChanged', slotProps.data)"> </Dropdown>
                    <!-- Calendar -->
                    <Calendar v-else-if="col.editable && col.columnInfo.type === 'date' && col.columnInfo.subType !== 'timestamp'" v-model="slotProps.data[col.field]" @date-select="$emit('rowChanged', slotProps.data)" />
                    <span v-else>TODO</span>
                </template>
                <template #body="slotProps">
                    <!-- Checkbox -->
                    <Checkbox v-if="col.editor == 'TEXT' && col.columnInfo.type === 'boolean'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @click="$emit('rowChanged', slotProps.data)" :disabled="!col.editable"></Checkbox>
                    <!-- Formating -->
                    <div v-else-if="col.editable" @click="addColumnOptions(col, slotProps.data)">
                        <!-- Calendar -->
                        <span v-if="col.columnInfo.type === 'int'">{{ formatNumber(slotProps.data[col.field]) ?? '' }}</span>
                        <span v-else-if="col.columnInfo.type === 'float'">{{ formatDecimalNumber(slotProps.data[col.field], col.columnInfo.format) }}</span>
                        <span v-else-if="col.columnInfo.type === 'date' && col.columnInfo.subType !== 'timestamp'">{{ getFormatedDate(slotProps.data[col.field]) }}</span>
                        <span v-else> {{ slotProps.data[col.field] }}</span>
                    </div>
                    <span v-else> {{ slotProps.data[col.field] }}</span>
                </template>
            </Column>
        </template>

        <Column :style="registryDatatableDescriptor.iconColumn.style">
            <template #body="slotProps">
                <Button icon="pi pi-trash" class="p-button-link" @click="rowDeleteConfirm(slotProps.data)" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { formatDate } from '@/helpers/commons/localeHelper'
import axios from 'axios'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import registryDatatableDescriptor from './RegistryDatatableDescriptor.json'

export default defineComponent({
    name: 'registry-datatable',
    components: { Calendar, Checkbox, Column, DataTable, Dropdown },
    props: { propColumns: { type: Array }, propRows: { type: Array }, columnMap: { type: Object }, propConfiguration: { type: Object } },
    emits: ['rowChanged', 'rowDeleted'],
    data() {
        return {
            registryDatatableDescriptor,
            columns: [] as any[],
            rows: [] as any[],
            configuration: {} as any,
            comboColumnOptions: [] as any[],
            buttons: {
                enableButtons: false,
                enableDeleteRecords: false,
                enableAddRecords: false
            }
        }
    },
    watch: {
        propColumns() {
            this.loadColumns()
        },
        propRows() {
            this.loadRows()
        },
        propConfiguration() {
            this.loadConfiguration()
        }
    },
    created() {
        this.loadColumns()
        this.loadRows()
        this.loadConfiguration()
        this.loadDropdownValues('store_type')
    },
    methods: {
        loadColumns() {
            this.columns = [{ field: 'id', title: '', size: '', visible: 'true', editable: 'false', columnInfo: { type: 'int' } }]
            this.propColumns?.forEach((el: any) => {
                if (el.visible) this.columns.push(el)
            })
            console.log('PROP COLUMN: ', this.propColumns)
            console.log('COLUMN: ', this.columns)
        },
        loadRows() {
            this.rows = [...(this.propRows as any[])]
            console.log('ROWS: ', this.rows)
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
                // console.log('CONFIGURATION: ', this.configuration)
                // console.log('BUTTONS: ', this.configuration)
            }
        },
        rowDeleteConfirm(row: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('rowDeleted', row)
            })
        },
        setDataType(columnType: string) {
            switch (columnType) {
                case 'int':
                case 'float':
                case 'decimal':
                case 'long':
                    return 'number'
                case 'date':
                    return 'date'
                default:
                    return 'text'
            }
        },
        getStep(dataType: string) {
            if (dataType === 'float') {
                return '.01'
            } else if (dataType === 'int') {
                return '1'
            } else {
                return 'any'
            }
        },
        isDependentColumn(column: any) {
            console.log('IS DEPENDENT ' + column + ' : ' + 'dependsFrom' in column)
            return 'dependsFrom' in column
        },
        getFormatedDate(date: any) {
            return formatDate(date, 'MM/DD/yyyy')
        },
        formatNumber(number: number) {
            // console.log('NUMBER: ', number)
            return number ? new Intl.NumberFormat('en-US', { maximumSignificantDigits: 3 }).format(number) : ''
        },
        formatDecimalNumber(number: number, format: string) {
            console.log('NUMBER: ', number, ', FORMAT: ', format)
        },
        addColumnOption(column: any, row: any) {
            row.selected = true

            //regular independent combo columns
            if (column.editor === 'COMBO' && !this.isDependentColumn(column)) {
                if (!this.comboColumnOptions[column.field]) {
                    this.comboColumnOptions[column.field] = {}
                    this.getData(column.field)
                }
            }

            //dependent combo columns
            if (column.editor === 'COMBO' && this.isDependentColumn(column)) {
                if (!this.comboColumnOptions[column.field]) {
                    this.comboColumnOptions[column.field] = {}

                    this.getDependenciesOptions(column, row)
                } else if (!((row[column.dependsFrom] as any) in this.comboColumnOptions[column.field])) {
                    this.getDependenciesOptions(column, row)
                }
            }
        },
        async getData(field: string) {
            console.log('getData', field)
            //  this.comboColumnOptions[field] = response;
        },
        getDependenciesOptions(column: any, row: any) {
            console.log('getDependenciesOptions', column, row)
            // this.comboColumnOptions[column.field][row[column.dependsFrom]] = response.data.rows
        },
        async loadDropdownValues(column: string) {
            await axios
                // .get(`knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=c75a32e00fbf11ec8b65ed57c30e47f4`)
                .get('../data/demo_dropdown_store_type.json')
                .then((response) => (this.comboColumnOptions[column] = response.data.rows))
        }
    }
})
</script>
