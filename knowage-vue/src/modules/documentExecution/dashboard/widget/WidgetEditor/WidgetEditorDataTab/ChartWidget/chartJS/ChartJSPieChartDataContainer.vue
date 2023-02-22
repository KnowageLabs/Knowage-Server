<template>
    <div v-if="widget">
        <WidgetEditorColumnTable
            class="p-m-2"
            :widget-model="widget"
            :items="columnTableItems['ATTRIBUTES'] ?? []"
            :settings="{ ...commonDescriptor.columnTableSettings, ...chartJSDescriptor.pieChartColumnTableSettings[0] }"
            chart-type="chartJSPieChart"
            @rowReorder="onColumnsReorder"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <WidgetEditorColumnTable
            class="p-m-2"
            :widget-model="widget"
            :items="columnTableItems['MEASURES'] ?? []"
            :settings="{ ...commonDescriptor.columnTableSettings, ...chartJSDescriptor.pieChartColumnTableSettings[1] }"
            chart-type="chartJSPieChart"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <ChartWidgetColumnForm class="p-m-2" :widget-model="widget" :selected-column="selectedColumn"></ChartWidgetColumnForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../../TableWidget/TableWidgetDataDescriptor.json'
import chartJSDescriptor from './ChartJSDataContainerDescriptor.json'
import commonDescriptor from '../../common/WidgetCommonDescriptor.json'
import WidgetEditorColumnTable from '../../common/WidgetEditorColumnTable.vue'
import ChartWidgetColumnForm from '../common/ChartWidgetColumnForm.vue'

export default defineComponent({
    name: 'chart-js-widget-pie-chart-data-container',
    components: { WidgetEditorColumnTable, ChartWidgetColumnForm },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor,
            widget: {} as IWidget,
            chartJSDescriptor,
            commonDescriptor,
            columnTableItems: {} as any,
            selectedColumn: null as IWidgetColumn | null
        }
    },
    watch: {
        selectedDataset() {
            this.selectedColumn = null
        }
    },
    async created() {
        this.loadWidget()
        this.$watch('widget.columns', () => this.loadColumnTableItems())
        this.loadColumnTableItems()
    },
    methods: {
        loadWidget() {
            this.widget = this.widgetModel
        },
        loadColumnTableItems() {
            this.columnTableItems = []
            this.columnTableItems['ATTRIBUTES'] = []
            this.columnTableItems['MEASURES'] = []
            this.widget.columns.forEach((column: IWidgetColumn) => {
                const type = column.fieldType == 'MEASURE' ? 'MEASURES' : 'ATTRIBUTES'
                if ((type === 'MEASURES' && this.columnTableItems['MEASURES'].length === 1) || (type === 'ATTRIBUTES' && this.columnTableItems['ATTRIBUTES'].length === 1)) return
                this.columnTableItems[type].push(column)
            })
        },
        onColumnsReorder(columns: IWidgetColumn[]) {
            this.columnTableItems['ATTRIBUTES'] = columns
            this.widget.columns = this.columnTableItems['ATTRIBUTES'].concat(this.columnTableItems['MEASURES'])
            emitter.emit('columnsReordered', this.widget.columns)
            emitter.emit('refreshWidgetWithData', this.widget.id)
        },
        onColumnAdded(payload: { column: IWidgetColumn; rows: IWidgetColumn[]; settings: any }) {
            if (!payload.rows) this.columnTableItems['MEASURES'] = [payload]
            else {
                const type = payload.settings?.measuresOnly ? 'MEASURES' : 'ATTRIBUTES'
                this.columnTableItems[type] = payload.rows
            }
            this.updateWidgetColumns()
        },
        updateWidgetColumns() {
            this.widget.columns = this.columnTableItems['ATTRIBUTES'].concat(this.columnTableItems['MEASURES'])
            emitter.emit('refreshWidgetWithData', this.widget.id)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            const index = this.widget.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widget.columns[index] = { ...column }
                emitter.emit('refreshWidgetWithData', this.widget.id)
                if (this.widget.columns[index].id === this.selectedColumn?.id) this.selectedColumn = { ...this.widget.columns[index] }
            }
        },
        setSelectedColumn(column: IWidgetColumn) {
            this.selectedColumn = { ...column }
        },
        onColumnDelete(column: IWidgetColumn) {
            const index = this.widget.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widget.columns.splice(index, 1)
                if (column.id === this.selectedColumn?.id) this.selectedColumn = null
                this.removeColumnFromColumnTableItems(column)
                emitter.emit('refreshWidgetWithData', this.widget.id)
            }
        },
        removeColumnFromColumnTableItems(column: IWidgetColumn) {
            const type = column.fieldType == 'MEASURE' ? 'MEASURES' : 'ATTRIBUTES'
            const index = this.columnTableItems[type].findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) this.columnTableItems[type].splice(index, 1)
        }
    }
})
</script>
