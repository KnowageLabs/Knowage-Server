<template>
    <div v-if="widgetModel">
        <TableWidgetDataForm :widgetModel="widgetModel" :sortingColumnOptions="columnTableItems"></TableWidgetDataForm>
        <WidgetEditorColumnTable
            :widgetModel="widgetModel"
            :items="columnTableItems"
            :settings="descriptor.columnTableSettings"
            @rowReorder="onColumnsReorder"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import TableWidgetDataForm from './TableWidgetDataForm.vue'
import WidgetEditorColumnTable from '../common/WidgetEditorColumnTable.vue'
import descriptor from './TableWidgetDescriptor.json'

export default defineComponent({
    name: 'table-widget-data-container',
    components: { TableWidgetDataForm, WidgetEditorColumnTable },
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
            console.log('LOADED WIDGET MODEL: ', this.widgetModel)
            this.columnTableItems = this.widgetModel.columns ?? []
        },
        onColumnsReorder(columns: IWidgetColumn[]) {
            this.widgetModel.columns = columns
            emitter.emit('columnsReordered', this.widgetModel.columns)
        },
        onColumnAdded(column: IWidgetColumn) {
            this.widgetModel.columns.push(column)
            emitter.emit('collumnAdded', column)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widgetModel.columns[index] = { ...column }
                emitter.emit('collumnUpdated', { column: this.widgetModel.columns[index], columnIndex: index })
            }
        },
        setSelectedColumn(column: IWidgetColumn) {
            this.selectedColumn = { ...column }
        },
        onColumnDelete(column: IWidgetColumn) {
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widgetModel.columns.splice(index, 1)
                emitter.emit('collumnRemoved', column)
            }
        }
    }
})
</script>
