<template>
    <div v-if="widgetModel">
        <TableWidgetDataForm class="p-m-2" :widgetModel="widgetModel" :sortingColumnOptions="columnTableItems"></TableWidgetDataForm>
        <WidgetEditorColumnTable
            class="p-m-2"
            :widgetModel="widgetModel"
            :items="columnTableItems"
            :settings="descriptor.columnTableSettings"
            @rowReorder="onColumnsReorder"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <TableWidgetColumnForm class="p-m-2" :widgetModel="widgetModel" :selectedColumn="selectedColumn"></TableWidgetColumnForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from './TableWidgetDataDescriptor.json'
import TableWidgetDataForm from './TableWidgetDataForm.vue'
import TableWidgetColumnForm from './TableWidgetColumnForm.vue'
import WidgetEditorColumnTable from '../common/WidgetEditorColumnTable.vue'

export default defineComponent({
    name: 'table-widget-data-container',
    components: { TableWidgetDataForm, TableWidgetColumnForm, WidgetEditorColumnTable },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            columnTableItems: [] as IWidgetColumn[],
            selectedColumn: null as IWidgetColumn | null
        }
    },
    async created() {
        this.$watch('widgetModel.columns', () => this.loadColumnTableItems())
        this.loadColumnTableItems()
    },
    methods: {
        loadColumnTableItems() {
            this.columnTableItems = this.widgetModel.columns ?? []
        },
        onColumnsReorder(columns: IWidgetColumn[]) {
            this.widgetModel.columns = columns
            emitter.emit('columnsReordered', this.widgetModel.columns)
        },
        onColumnAdded(column: IWidgetColumn) {
            this.widgetModel.columns.push(column)
            emitter.emit('columnAdded', column)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widgetModel.columns[index] = { ...column }
                emitter.emit('collumnUpdated', { column: this.widgetModel.columns[index], columnIndex: index })
                if (this.widgetModel.columns[index].id === this.selectedColumn?.id) this.selectedColumn = { ...this.widgetModel.columns[index] }
            }
        },
        setSelectedColumn(column: IWidgetColumn) {
            this.selectedColumn = { ...column }
        },
        onColumnDelete(column: IWidgetColumn) {
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widgetModel.columns.splice(index, 1)
                if (column.id === this.selectedColumn?.id) this.selectedColumn = null
                emitter.emit('columnRemoved', column)
            }
        }
    }
})
</script>
