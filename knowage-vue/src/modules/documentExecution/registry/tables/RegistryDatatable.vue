<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #right>
                    <div class="p-d-flex p-flex-row">
                        <Button class="kn-button p-button-text" :label="$t('managers.businessModelManager.add')" v-if="buttons.enableButtons || buttons.enableAddRecords" @click="addNewRow" data-test="new-row-button" />
                    </div>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <div class="col-12 scrollable-table">
                <DataTable
                    class="p-datatable-sm kn-table"
                    v-model:first="first"
                    :value="rows"
                    editMode="cell"
                    dataKey="id"
                    paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
                    :lazy="lazyParams.size > registryDescriptor.paginationLimit"
                    :paginator="true"
                    :rows="registryDescriptor.paginationNumberOfItems"
                    :currentPageReportTemplate="
                        $t('common.table.footer.paginated', {
                            first: '{first}',
                            last: '{last}',
                            totalRecords: '{totalRecords}'
                        })
                    "
                    :totalRecords="lazyParams.size"
                    responsiveLayout="stack"
                    breakpoint="960px"
                    stripedRows
                    showGridlines
                    @page="onPage($event)"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <Column class="kn-truncated" :style="registryDatatableDescriptor.numerationColumn.style" :field="columns[0].field" :header="columns[0].title"></Column>
                    <template v-for="col of columns.slice(1)" :key="col.field">
                        <Column
                            class="kn-truncated"
                            style="width:200px"
                            :field="col.field"
                            :style="col.columnInfo.type === 'date' ? registryDatatableDescriptor.dateColumn.style : ''"
                            :bodyStyle="{
                                'background-color': col.color,
                                width: col.size + 'px'
                            }"
                        >
                            <template #header>
                                <div class="table-header">
                                    {{ col.title }}
                                    <i v-if="col.isEditable && col.columnInfo.type !== 'boolean'" class="pi pi-pencil edit-icon p-ml-2" :data-test="col.field + '-icon'" />
                                </div>
                            </template>
                            <template #editor="slotProps">
                                <div :data-test="col.field + '-editor'">
                                    <span v-if="!col.isEditable && col.columnInfo.type !== 'boolean'">{{ slotProps.data[col.field] }}</span>
                                    <Checkbox v-else-if="col.editorType === 'TEXT' && col.columnInfo.type === 'boolean'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="setRowEdited(slotProps.data)" :disabled="!col.isEditable"></Checkbox>
                                    <RegistryDatatableEditableField
                                        v-else-if="col.isEditable"
                                        :column="col"
                                        :propRow="slotProps.data"
                                        :comboColumnOptions="comboColumnOptions"
                                        @rowChanged="setRowEdited(slotProps.data)"
                                        @dropdownChanged="onDropdownChange"
                                        @dropdownOpened="addColumnOptions"
                                    ></RegistryDatatableEditableField>
                                </div>
                            </template>
                            <template #body="slotProps">
                                <div class="p-d-flex p-flex-row" :data-test="col.field + '-body'">
                                    <Checkbox v-if="col.editorType == 'TEXT' && col.columnInfo.type === 'boolean'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="setRowEdited(slotProps.data)" :disabled="!col.isEditable"></Checkbox>
                                    <Calendar
                                        :style="registryDatatableDescriptor.pivotStyles.inputFields"
                                        class="pivot-calendar"
                                        v-else-if="col.isEditable && col.columnInfo.type === 'date'"
                                        v-model="slotProps.data[col.field]"
                                        :showTime="col.columnInfo.subtype === 'timestamp'"
                                        :showSeconds="col.columnInfo.subtype === 'timestamp'"
                                        :dateFormat="col.columnInfo.dateFormat"
                                        :showButtonBar="true"
                                        @date-select="setRowEdited(slotProps.data)"
                                    />
                                    <div v-else-if="col.isEditable">
                                        <span v-if="(col.columnInfo.type === 'int' || col.columnInfo.type === 'float') && slotProps.data[col.field]">{{ getFormatedNumber(slotProps.data[col.field]) }}</span>
                                        <span v-else> {{ slotProps.data[col.field] }}</span>
                                    </div>
                                    <span v-else-if="col.columnInfo.type === 'date'"> {{ getFormatedDate(slotProps.data[col.field], col.columnInfo.dateFormat) }}</span>
                                    <span v-else> {{ slotProps.data[col.field] }}</span>
                                </div>
                            </template>
                        </Column>
                    </template>
                    <Column :style="registryDatatableDescriptor.iconColumn.style" :headerStyle="registryDatatableDescriptor.headerIconColumn.style">
                        <template #body="slotProps">
                            <Button v-if="buttons.enableButtons || buttons.enableDeleteRecords" class="p-button-link" @click="rowDeleteConfirm(slotProps.index, slotProps.data)">
                                <i class="pi pi-flag" :class="[slotProps.data.edited ? flagShown : flagHidden]" :style="registryDatatableDescriptor.primevueTableStyles.trashNormal" />
                                <i class="p-button-link pi pi-trash p-ml-2" :style="registryDatatableDescriptor.primevueTableStyles.trashNormal" />
                            </Button>
                        </template>
                    </Column>
                </DataTable>
            </div>
        </template>
    </Card>

    <RegistryDatatableWarningDialog :visible="warningVisible" :columns="dependentColumns" @close="onWarningDialogClose"></RegistryDatatableWarningDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { formatDateWithLocale, formatNumberWithLocale } from '@/helpers/commons/localeHelper'
import axios from 'axios'
import Calendar from 'primevue/calendar'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import registryDescriptor from '../RegistryDescriptor.json'
import registryDatatableDescriptor from './RegistryDatatableDescriptor.json'
import RegistryDatatableEditableField from './RegistryDatatableEditableField.vue'
import RegistryDatatableWarningDialog from './RegistryDatatableWarningDialog.vue'

export default defineComponent({
    name: 'registry-datatable',
    components: {
        Card,
        Calendar,
        Checkbox,
        Column,
        DataTable,
        RegistryDatatableEditableField,
        RegistryDatatableWarningDialog
    },
    props: {
        propColumns: { type: Array },
        propRows: { type: Array, required: true },
        columnMap: { type: Object },
        propConfiguration: { type: Object },
        pagination: { type: Object },
        entity: { type: String },
        id: { type: String }
    },
    emits: ['rowChanged', 'rowDeleted', 'pageChanged'],
    data() {
        return {
            registryDescriptor,
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
            selectedRow: null as any,
            warningVisible: false,
            stopWarnings: [] as any[],
            flagShown: 'flag-shown',
            flagHidden: 'flag-hidden',
            first: 0
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
                this.first = this.pagination?.start
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
            this.columns = [
                {
                    field: 'id',
                    title: '',
                    size: '',
                    isVisible: true,
                    isEditable: false,
                    columnInfo: { type: 'int' }
                }
            ]
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
                        this.comboColumnOptions[column.dependences] = []
                    }
                }
            })
        },
        loadRows() {
            this.rows = [...(this.propRows as any[])]
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
        rowDeleteConfirm(index: number, row: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteRow(index, row)
            })
        },
        deleteRow(index: number, row: any) {
            row.isNew ? this.rows.splice(index, 1) : this.$emit('rowDeleted', row)
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
        getFormatedDate(date: any, format: any) {
            return formatDateWithLocale(date, format)
        },
        getFormatedNumber(number: number, precision?: number, format?: any) {
            return formatNumberWithLocale(number, precision, format)
        },
        addColumnOptions(payload: any) {
            const column = payload.column
            const row = payload.row

            if (!this.comboColumnOptions[column.field]) {
                this.comboColumnOptions[column.field] = []
            }

            if (!this.comboColumnOptions[column.field][row[column.dependences]]) {
                this.loadColumnOptions(column, row)
            }
        },
        async loadColumnOptions(column: any, row: any) {
            const subEntity = column.subEntity ? '::' + column.subEntity + '(' + column.foreignKey + ')' : ''

            const entityId = this.entity + subEntity + ':' + column.field
            const entityOrder = this.entity + subEntity + ':' + (column.orderBy ?? column.field)

            const postData = new URLSearchParams({
                ENTITY_ID: entityId,
                QUERY_TYPE: 'standard',
                ORDER_ENTITY: entityOrder,
                ORDER_TYPE: 'asc',
                QUERY_ROOT_ENTITY: 'true'
            })
            if (column.dependences && row && row[column.dependences]) {
                postData.append('DEPENDENCES', this.entity + subEntity + ':' + column.dependences + '=' + row[column.dependences])
            }
            await axios
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then((response) => (this.comboColumnOptions[column.field][row[column.dependences]] = response.data.rows))
        },
        addNewRow() {
            const newRow = { id: this.rows.length, isNew: true }
            this.columns.forEach((el: any) => {
                if (el.isVisible && el.field !== 'id') {
                    newRow[el.field] = el.defaultValue ?? ''
                }
            })
            this.rows.unshift(newRow)

            if (this.lazyParams.size <= registryDescriptor.paginationLimit) {
                this.first = 0
            }
            this.$emit('rowChanged', newRow)
        },
        onDropdownChange(payload: any) {
            const column = payload.column
            const row = payload.row

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
        onWarningDialogClose(payload: any) {
            if (payload.stopWarnings) {
                this.stopWarnings[payload.columnField] = true
            }

            this.clearDependentColumnsValues()
            this.warningVisible = false
        },
        clearDependentColumnsValues() {
            this.dependentColumns.forEach((el: any) => (this.selectedRow[el.field] = ''))
            this.selectedRow.edited = true
            this.$emit('rowChanged', this.selectedRow)
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
        setRowEdited(row: any) {
            row.edited = true
            this.$emit('rowChanged', row)
        }
    }
})
</script>
<style lang="scss">
.flag-shown {
    opacity: 1;
}
.flag-hidden {
    opacity: 0;
}
.scrollable-table .p-datatable-wrapper {
    max-width: 93vw;
    overflow-x: auto;
}
.scrollable-table .p-datatable {
    max-width: 93vw;
}
</style>
