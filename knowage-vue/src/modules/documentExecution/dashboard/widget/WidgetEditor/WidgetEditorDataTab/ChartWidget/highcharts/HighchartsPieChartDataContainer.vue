<template>
    <div v-if="widgetModel">
        {{ 'WIDGET COLUMNS' }}
        <br />
        {{ widgetModel.columns }}
        <br />
        {{ 'ATTRIBUTES' }}
        <br />
        {{ columnTableItems['ATTRIBUTES'] }}
        <br />
        {{ 'MEASURES' }}
        <br />
        {{ columnTableItems['MEASURES'] }}
        <WidgetEditorColumnTable
            class="p-m-2"
            :widgetModel="widgetModel"
            :items="columnTableItems['ATTRIBUTES'] ?? []"
            :settings="{ ...commonDescriptor.columnTableSettings, ...highchartDescriptor.pieChartcolumnTableSettings[0] }"
            chartType="highchartsPieChart"
            @rowReorder="onColumnsReorder"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <WidgetEditorColumnTable
            class="p-m-2"
            :widgetModel="widgetModel"
            :items="columnTableItems['MEASURES'] ?? []"
            :settings="{ ...commonDescriptor.columnTableSettings, ...highchartDescriptor.pieChartcolumnTableSettings[1] }"
            chartType="highchartsPieChart"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <HighchartsWidgetColumnForm class="p-m-2" :widgetModel="widgetModel" :selectedColumn="selectedColumn"></HighchartsWidgetColumnForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../../TableWidget/TableWidgetDataDescriptor.json'
import highchartDescriptor from './HighchartsWidgetDataContainerDescriptor.json'
import Dropdown from 'primevue/dropdown'
import commonDescriptor from '../../common/WidgetCommonDescriptor.json'
import WidgetEditorColumnTable from '../../common/WidgetEditorColumnTable.vue'
import WidgetEditorFilterForm from '../../common/WidgetEditorFilterForm.vue'
import HighchartsWidgetColumnForm from './common/HighchartsWidgetColumnForm.vue'

export default defineComponent({
    name: 'highcharts-widget-pie-chart-data-container',
    components: { Dropdown, WidgetEditorColumnTable, WidgetEditorFilterForm, HighchartsWidgetColumnForm },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor,
            highchartDescriptor,
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
        this.$watch('widgetModel.columns', () => this.loadColumnTableItems())
        this.loadColumnTableItems()
    },
    methods: {
        loadColumnTableItems() {
            this.columnTableItems = []
            this.columnTableItems['ATTRIBUTES'] = []
            this.columnTableItems['MEASURES'] = []
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                const type = column.fieldType == 'MEASURE' ? 'MEASURES' : 'ATTRIBUTES'
                if (type === 'MEASURES' && this.columnTableItems['MEASURES'].length === 1) return
                this.columnTableItems[type].push(column)
            })
        },
        onColumnsReorder(columns: IWidgetColumn[]) {
            this.columnTableItems['ATTRIBUTES'] = columns
            this.widgetModel.columns = this.columnTableItems['ATTRIBUTES'].concat(this.columnTableItems['MEASURES'])
            emitter.emit('columnsReordered', this.widgetModel.columns)
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        onColumnAdded(payload: { column: IWidgetColumn; rows: IWidgetColumn[]; settings: any }) {
            const type = payload.settings?.measuresOnly ? 'MEASURES' : 'ATTRIBUTES'
            this.columnTableItems[type] = payload.rows
            this.widgetModel.columns = this.columnTableItems['ATTRIBUTES'].concat(this.columnTableItems['MEASURES'])
            emitter.emit('columnAdded', payload.column)
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widgetModel.columns[index] = { ...column }
                emitter.emit('collumnUpdated', { column: this.widgetModel.columns[index], columnIndex: index })
                emitter.emit('refreshWidgetWithData', this.widgetModel.id)
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
                this.removeColumnFromColumnTableItems(column)

                // removeColumnFromModel(this.widgetModel, column)
                emitter.emit('columnRemoved', column)
                emitter.emit('refreshWidgetWithData', this.widgetModel.id)
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
