<template>
    <div v-if="widgetModel">
        <WidgetEditorColumnTable class="p-m-2" :widgetModel="widgetModel" :items="columnTableItems" :settings="descriptor.columnTableSettings" @itemAdded="onColumnAdded" @itemUpdated="onColumnItemUpdate" @itemSelected="setSelectedColumn" @itemDeleted="onColumnDelete"></WidgetEditorColumnTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { removeColumnFromModel } from '../../helpers/selectorWidget/SelectorWidgetFunctions'
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
            if (this.widgetModel.columns.length > 0) {
                emitter.emit('columnRemoved', this.widgetModel.columns[0])
            }
            this.widgetModel.columns = [column]
            emitter.emit('columnAdded', column)
            emitter.emit('refreshSelector', this.widgetModel.id)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            this.widgetModel.columns[0] = { ...column }
            emitter.emit('collumnUpdated', { column: this.widgetModel.columns[0], columnIndex: 0 })
            emitter.emit('refreshSelector', this.widgetModel.id)
            if (this.widgetModel.columns[0].id === this.selectedColumn?.id) this.selectedColumn = { ...this.widgetModel.columns[0] }
        },
        setSelectedColumn(column: IWidgetColumn) {
            this.selectedColumn = { ...column }
        },
        onColumnDelete(column: IWidgetColumn) {
            this.widgetModel.columns = []
            if (column.id === this.selectedColumn?.id) this.selectedColumn = null
            removeColumnFromModel(this.widgetModel, column)
            emitter.emit('columnRemoved', column)
            emitter.emit('refreshSelector', this.widgetModel.id)
        }
    }
})
</script>
