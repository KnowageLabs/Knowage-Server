<template>
    <div v-if="widgetModel">
        <TableWidgetDataForm v-if="widgetType !== 'discovery'" class="p-m-2" :widget-model="widgetModel" :sorting-column-options="columnTableItems"></TableWidgetDataForm>
        <WidgetEditorColumnTable
            class="p-m-2"
            :widget-model="widgetModel"
            :items="columnTableItems"
            :settings="descriptor.columnTableSettings"
            @rowReorder="onColumnsReorder"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <TableWidgetColumnForm class="p-m-2" :widget-model="widgetModel" :selected-column="selectedColumn"></TableWidgetColumnForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { removeColumnFromTableWidgetModel } from '../../helpers/tableWidget/TableWidgetFunctions'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from './WidgetCommonDescriptor.json'
import TableWidgetDataForm from '../TableWidget/TableWidgetDataForm.vue'
import TableWidgetColumnForm from '../TableWidget/TableWidgetColumnForm.vue'
import WidgetEditorColumnTable from './WidgetEditorColumnTable.vue'

export default defineComponent({
    name: 'widget-editor-common-data-container',
    components: { TableWidgetDataForm, TableWidgetColumnForm, WidgetEditorColumnTable },
    props: { propWidgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor,
            widgetModel: {} as IWidget,
            columnTableItems: [] as IWidgetColumn[],
            selectedColumn: null as IWidgetColumn | null
        }
    },
    computed: {
        widgetType() {
            return this.widgetModel.type
        }
    },
    watch: {
        propWidgetModel() {
            this.loadWidgetModel()
        },
        selectedDataset() {
            this.selectedColumn = null
        }
    },
    async created() {
        this.loadWidgetModel()
        this.$watch('widgetModel.columns', () => this.loadColumnTableItems())
        this.loadColumnTableItems()
    },
    methods: {
        loadWidgetModel() {
            this.widgetModel = this.propWidgetModel
        },
        loadColumnTableItems() {
            this.columnTableItems = this.widgetModel.columns ?? []
        },
        onColumnsReorder(columns: IWidgetColumn[]) {
            this.widgetModel.columns = columns
            emitter.emit('columnsReordered', this.widgetModel.columns)
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        onColumnAdded(payload: { column: IWidgetColumn; rows: IWidgetColumn[] }) {
            this.widgetModel.columns = payload.rows
            // if (this.widgetType === 'discovery') addColumnToDiscoveryWidgetModel(this.widgetModel, payload.column)
            emitter.emit('columnAdded', payload.column)
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (this.widgetType === 'discovery' && column.fieldType === 'ATTRIBUTE') {
                this.clearDiscoveryWidgetAggregatedColumnValuesForSpecificColumn(column)
            }
            if (index !== -1) {
                this.widgetModel.columns[index] = { ...column }
                emitter.emit('collumnUpdated', { column: this.widgetModel.columns[index], columnIndex: index })
                emitter.emit('refreshWidgetWithData', this.widgetModel.id)
                if (this.widgetModel.columns[index].id === this.selectedColumn?.id) this.selectedColumn = { ...this.widgetModel.columns[index] }
            }
            this.loadColumnTableItems()
        },
        setSelectedColumn(column: IWidgetColumn) {
            this.selectedColumn = { ...column }
        },
        onColumnDelete(column: IWidgetColumn) {
            if (column.id === this.selectedColumn?.id) this.selectedColumn = null
            this.removeColumnFromModel(column)
            emitter.emit('columnRemoved', column)
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        removeColumnFromModel(column: IWidgetColumn) {
            switch (this.widgetType) {
                case 'table':
                    removeColumnFromTableWidgetModel(this.widgetModel, column)
                    break
                case 'discovery':
                    if (column.fieldType === 'MEASURE') this.clearDiscoveryWidgetAggregatedColumnValuesForSpecificColumn(column)
                // removeColumnFromDiscoveryWidgetModel(this.widgetModel, column)
            }
        },
        clearDiscoveryWidgetAggregatedColumnValuesForSpecificColumn(column: IWidgetColumn) {
            this.widgetModel.columns.forEach((tempColumn: IWidgetColumn) => {
                if (tempColumn.aggregationColumn === column?.columnName) {
                    tempColumn.aggregation = 'COUNT'
                    tempColumn.aggregationColumn = ''
                }
            })
        }
    }
})
</script>
