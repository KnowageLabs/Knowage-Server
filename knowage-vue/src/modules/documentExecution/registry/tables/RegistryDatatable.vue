<template>
    <Card>
        <template #content>
            <DataTable class="p-datatable-sm kn-table" :value="rows" editMode="cell" dataKey="id" :lazy="this.lazyParams.size > 1000" :paginator="true" :rows="15" :totalRecords="lazyParams.size" responsiveLayout="stack" breakpoint="960px" @page="onPage($event)">
                <template v-for="col of columns" :key="col.field">
                    <Column class="kn-truncated" :field="col.field" :header="col.title">
                        <template #editor="slotProps">
                            <span v-if="!col.isEditable && col.columnInfo.type !== 'boolean'">{{ slotProps.data[col.field] }}</span>
                            <!-- Checkbox -->
                            <Checkbox v-else-if="col.editorType === 'TEXT' && col.columnInfo.type === 'boolean'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @click="$emit('rowChanged', slotProps.data)" :disabled="!col.isEditable"></Checkbox>
                            <InputText
                                v-else-if="col.editorType !== 'COMBO' && col.isEditable && col.columnInfo.type !== 'date'"
                                class="p-inputtext-sm"
                                :type="setDataType(col.columnInfo.type)"
                                :step="getStep(col.columnInfo.type)"
                                v-model="slotProps.data[slotProps.column.props.field]"
                                @input="$emit('rowChanged', slotProps.data)"
                            />
                            <!-- Dropdown -->
                            <Dropdown
                                v-else-if="col.editorType === 'COMBO'"
                                v-model="slotProps.data[col.field]"
                                :options="this.comboColumnOptions[col.field]"
                                optionValue="column_1"
                                optionLabel="column_1"
                                @change="onDropdownChange(slotProps.data, col)"
                                @before-show="addColumnOptions(col, slotProps.data)"
                            >
                            </Dropdown>
                            <!-- Calendar -->
                            <Calendar v-else-if="col.isEditable && col.columnInfo.type === 'date' && col.columnInfo.subType !== 'timestamp'" v-model="slotProps.data[col.field]" @date-select="$emit('rowChanged', slotProps.data)" />
                            <span v-else>TODO</span>
                        </template>
                        <template #body="slotProps">
                            <!-- Checkbox -->
                            <Checkbox v-if="col.editorType == 'TEXT' && col.columnInfo.type === 'boolean'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @click="$emit('rowChanged', slotProps.data)" :disabled="!col.isEditable"></Checkbox>
                            <!-- Formating -->
                            <div v-else-if="col.isEditable" @click="addColumnOptions(col, slotProps.data)">
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

                <Column :style="registryDatatableDescriptor.iconColumn.style" :headerStyle="registryDatatableDescriptor.headerIconColumn.style">
                    <template #header>
                        <KnFabButton icon="fas fa-plus" @click="addNewRow"></KnFabButton>
                    </template>
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="rowDeleteConfirm(slotProps.index, slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>

    <RegistryDatatableWarningDialog :visible="warningVisible" :columns="dependentColumns" @close="onWarningDialogClose"></RegistryDatatableWarningDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { formatDate } from '@/helpers/commons/localeHelper'
import axios from 'axios'
import Calendar from 'primevue/calendar'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import registryDatatableDescriptor from './RegistryDatatableDescriptor.json'
import RegistryDatatableWarningDialog from './RegistryDatatableWarningDialog.vue'

export default defineComponent({
    name: 'registry-datatable',
    components: { Card, Calendar, Checkbox, Column, DataTable, Dropdown, KnFabButton, RegistryDatatableWarningDialog },
    props: { propColumns: { type: Array }, propRows: { type: Array }, columnMap: { type: Object }, propConfiguration: { type: Object }, pagination: { type: Object }, entity: { type: String }, id: { type: String } },
    emits: ['rowChanged', 'rowDeleted', 'pageChanged'],
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
            },
            lazyParams: {} as any,
            dependentColumns: [] as any[],
            warningVisible: false,
            stopWarnings: [] as any[]
        }
    },
    watch: {
        propColumns() {
            this.loadColumns()
        },
        propRows: {
            handler() {
                this.loadRows()
            },
            deep: true
        },
        propConfiguration() {
            this.loadConfiguration()
        },
        pagination: {
            handler() {
                this.loadPagination()
            },
            deep: true
        }
    },
    created() {
        this.loadColumns()
        this.loadRows()
        this.loadConfiguration()
        this.loadPagination()
    },
    methods: {
        loadColumns() {
            this.columns = [{ field: 'id', title: '', size: '', isVisible: true, isEditable: false, columnInfo: { type: 'int' } }]
            this.propColumns?.forEach((el: any) => {
                if (el.isVisible) this.columns.push(el)
            })
            this.setColumnDependencies()
        },
        setColumnDependencies() {
            this.columns.forEach((column: any) => {
                if (column.dependences) {
                    const index = this.columns.findIndex((parentColumn: any) => parentColumn.field === column.dependences)
                    if (index !== -1) {
                        this.columns[index].hasDependencies ? this.columns[index].hasDependencies.push(column) : (this.columns[index].hasDependencies = [column])
                    }
                }
            })
            console.log('COLUMNS: ', this.columns)
        },
        loadRows() {
            this.rows = [...(this.propRows as any[])]
            // console.log('ROWS: ', this.rows)
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
        loadPagination() {
            this.lazyParams = { ...this.pagination } as any
            console.log('LAZY PARAMS LOADED: ', this.lazyParams)
        },
        onPage(event: any) {
            this.lazyParams = { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows, size: this.lazyParams.size }
            this.$emit('pageChanged', this.lazyParams)
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
            row.id ? this.$emit('rowDeleted', row) : this.rows.splice(index, 1)
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
        addColumnOptions(column: any, row: any) {
            console.log('COLUMN: ', column, ', ROW: ', row)

            this.loadColumnOptions(column, row)

            // //regular independent combo columns
            // if (column.editorType === 'COMBO' && !this.isDependentColumn(column)) {
            //     if (!this.comboColumnOptions[column.field]) {
            //         this.comboColumnOptions[column.field] = {}
            //         this.getData(column.field)
            //     }
            // }

            // //dependent combo columns
            // if (column.editorType === 'COMBO' && this.isDependentColumn(column)) {
            //     if (!this.comboColumnOptions[column.field]) {
            //         this.comboColumnOptions[column.field] = {}

            //         this.getDependenciesOptions(column, row)
            //     } else if (!((row[column.dependsFrom] as any) in this.comboColumnOptions[column.field])) {
            //         this.getDependenciesOptions(column, row)
            //     }
            // }
        },
        // TODO izdvojiti u helper?
        async loadColumnOptions(column: any, row: any) {
            const postData = new URLSearchParams()
            const subEntity = column.subEntity ? '::' + column.subEntity + '(' + column.foreignKey + ')' : ''

            const entityId = this.entity + subEntity + ':' + column.field
            const entityOrder = this.entity + subEntity + ':' + (column.orderBy ?? column.field)

            postData.append('ENTITY_ID', entityId) // it.eng.knowage.meta.stores_for_registry.Store::rel_region_id_in_region(rel_region_id_in_region):sales_city
            postData.append('QUERY_TYPE', 'standard') //
            postData.append('ORDER_ENTITY', entityOrder) // it.eng.knowage.meta.stores_for_registry.Store::rel_region_id_in_region(rel_region_id_in_region):sales_city
            postData.append('ORDER_TYPE', 'asc')
            postData.append('QUERY_ROOT_ENTITY', 'true')
            postData.append('query', '')
            if (column.dependences && row) {
                postData.append('DEPENDENCES', this.entity + ':' + column.dependences + '=' + row[column.dependences])
            }
            await axios.post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then((response) => (this.comboColumnOptions[column.field] = response.data.rows))
            console.log('DROPDOWN VALUES: ', this.comboColumnOptions[column.field])
        },
        addNewRow() {
            const newRow = {}
            this.columns.forEach((el: any) => {
                if (el.isVisible && el.field !== 'id') {
                    newRow[el.field] = el.defaultValue ?? ''
                }
            })
            console.log('NEW ROW: ', newRow)
            this.rows.unshift(newRow)
            this.$emit('rowChanged', newRow)
        },
        onDropdownChange(row: any, column: any) {
            console.log('COLUMN: ', column)
            if (column.hasDependencies && !this.stopWarnings[column.field]) {
                this.dependentColumns = [] as any[]
                this.setDependentColumns(column)

                console.log('TEEEEEEEEEEEEST: ', this.dependentColumns)
                //this.dependentColumns = this.setDependentColumns(column) as any[]
                this.warningVisible = true
            }

            // console.log('WARNING VISIBLE: ', this.warningVisible)
            this.$emit('rowChanged', row)
        },
        onWarningDialogClose(payload: any) {
            this.warningVisible = false
            // console.log('STOP WARNINGS: ', payload.stopWarnings)
            if (payload.stopWarnings) {
                this.stopWarnings[payload.columnField] = true
            }
            // console.log('STOP WARINGGS:', this.stopWarnings)
        },
        setDependentColumns(column: any) {
            let tempColumn = column

            if (!tempColumn.hasDependencies) {
                return
            }

            // console.log('DEPENDENCIES: ', tempColumn.hasDependencies)
            tempColumn.hasDependencies.forEach((el: any) => {
                // console.log('DEP COLUMNS: ', el)
                this.dependentColumns.push(el)
                this.setDependentColumns(el)
            })
        }
    }
})
</script>
