<template>
    <div v-if="widgetModel">
        <WidgetEditorColumnTable class="p-m-2" :widgetModel="widgetModel" :items="columnTableItems" :settings="descriptor.columnTableSettings" @itemAdded="onColumnAdded" @itemUpdated="onColumnItemUpdate" @itemSelected="setSelectedColumn" @itemDeleted="onColumnDelete"></WidgetEditorColumnTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { removeColumnFromModel } from '../../helpers/tableWidget/TableWidgetFunctions'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from '../TableWidget/TableWidgetDataDescriptor.json'
import WidgetEditorColumnTable from '../common/WidgetEditorColumnTable.vue'

export default defineComponent({
    name: 'selector-widget-data-container',
    components: { WidgetEditorColumnTable },
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
        onColumnAdded(column: IWidgetColumn) {
            this.widgetModel.columns.push(column)
            emitter.emit('columnAdded', column)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            // TODO
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widgetModel.columns[index] = { ...column }
                emitter.emit('collumnUpdated', { column: this.widgetModel.columns[index], columnIndex: index })
                emitter.emit('refreshSelector', this.widgetModel.id)
                if (this.widgetModel.columns[index].id === this.selectedColumn?.id) this.selectedColumn = { ...this.widgetModel.columns[index] }
            }
        },
        setSelectedColumn(column: IWidgetColumn) {
            this.selectedColumn = { ...column }
        },
        onColumnDelete(column: IWidgetColumn) {
            // TODO
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widgetModel.columns.splice(index, 1)
                if (column.id === this.selectedColumn?.id) this.selectedColumn = null
                removeColumnFromModel(this.widgetModel, column)
                emitter.emit('columnRemoved', column)
                emitter.emit('refreshSelector', this.widgetModel.id)
            }
        }
    }
})
</script>
