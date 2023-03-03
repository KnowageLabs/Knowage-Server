<template>
    <div v-if="widgetModel">
        <WidgetEditorColumnTable
            v-if="chartType === 'pie'"
            class="p-m-2"
            :widget-model="widgetModel"
            :items="columnTableItems['ATTRIBUTES'] ?? []"
            :settings="{ ...commonDescriptor.columnTableSettings, ...highchartDescriptor.pieChartcolumnTableSettings[0] }"
            :chart-type="chartType"
            @rowReorder="onColumnsReorder($event, 'ATTRIBUTES')"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <WidgetEditorColumnTable
            class="p-m-2"
            :widget-model="widgetModel"
            :items="columnTableItems['MEASURES'] ?? []"
            :settings="valuesColumnSettings"
            :chart-type="chartType"
            @rowReorder="onColumnsReorder($event, 'MEASURES')"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <ChartWidgetColumnForm class="p-m-2" :widget-model="widgetModel" :selected-column="selectedColumn"></ChartWidgetColumnForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { removeSerieFromWidgetModel } from '../../../helpers/chartWidget/highcharts/HighchartsDataTabHelpers'
import descriptor from '../../TableWidget/TableWidgetDataDescriptor.json'
import highchartDescriptor from './HighchartsDataContainerDescriptor.json'
import commonDescriptor from '../../common/WidgetCommonDescriptor.json'
import WidgetEditorColumnTable from '../../common/WidgetEditorColumnTable.vue'
import ChartWidgetColumnForm from '../common/ChartWidgetColumnForm.vue'

export default defineComponent({
    name: 'highcharts-widget-common-data-container',
    components: { WidgetEditorColumnTable, ChartWidgetColumnForm },
    props: { propWidgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor,
            widgetModel: {} as IWidget,
            highchartDescriptor,
            commonDescriptor,
            columnTableItems: {} as any,
            selectedColumn: null as IWidgetColumn | null
        }
    },
    computed: {
        chartType() {
            return this.widgetModel?.settings.chartModel?.model?.chart.type
        },
        valuesColumnSettings() {
            switch (this.chartType) {
                case 'pie':
                    return { ...commonDescriptor.columnTableSettings, ...highchartDescriptor.pieChartcolumnTableSettings[1] }
                case 'gauge':
                    return { ...commonDescriptor.columnTableSettings, ...highchartDescriptor.gaugeChartcolumnTableSettings[0] }
                case 'activitygauge':
                    return { ...commonDescriptor.columnTableSettings, ...highchartDescriptor.activityGaugeChartcolumnTableSettings[0] }
                case 'solidgauge':
                    return { ...commonDescriptor.columnTableSettings, ...highchartDescriptor.solidGaugeChartcolumnTableSettings[0] }
                default:
                    return { ...commonDescriptor.columnTableSettings, ...highchartDescriptor.gaugeChartcolumnTableSettings[0] }
            }
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
            this.columnTableItems = []
            this.columnTableItems['ATTRIBUTES'] = []
            this.columnTableItems['MEASURES'] = []
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                const type = column.fieldType == 'MEASURE' ? 'MEASURES' : 'ATTRIBUTES'
                const maxNumberOfDimensions = this.chartType === 'highchartsPieChart' ? 1 : null
                if (type === 'MEASURES' && maxNumberOfDimensions && this.columnTableItems['MEASURES'].length === maxNumberOfDimensions) return
                this.columnTableItems[type].push(column)
            })
        },
        onColumnsReorder(columns: IWidgetColumn[], type: 'ATTRIBUTES' | 'MEASURES') {
            this.columnTableItems[type] = columns
            this.widgetModel.columns = this.columnTableItems['ATTRIBUTES'].concat(this.columnTableItems['MEASURES'])
            emitter.emit('columnsReordered', this.widgetModel.columns)
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
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
            this.widgetModel.columns = this.columnTableItems['ATTRIBUTES'].concat(this.columnTableItems['MEASURES'])
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                this.widgetModel.columns[index] = { ...column }
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
                removeSerieFromWidgetModel(this.widgetModel, column, this.chartType)
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
