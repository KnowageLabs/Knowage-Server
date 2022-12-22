<template>
    <Pie :style="myStyles" :chart-options="chartOptions" :chart-data="chartData" :chart-id="'pie-chart'" :dataset-id-key="'label'" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale } from 'chart.js'
import { IWidget, IWidgetColumn, ISelection } from '../../../Dashboard'
import { IChartJSChartModel, IChartJSData, IChartJSOptions } from '../../../interfaces/chartJS/DashboardChartJSWidget'
import { mapActions } from 'pinia'
import { updateStoreSelections } from '../../interactionsHelpers/InteractionHelper'
import store from '../../../Dashboard.store'

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale)

export default defineComponent({
    name: 'chartJS-container',
    components: { Pie },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, dataToShow: { type: Object as any, required: true }, dashboardId: { type: String, required: true }, editorMode: { type: Boolean }, propActiveSelections: { type: Array as PropType<ISelection[]>, required: true } },
    computed: {
        myStyles(): any {
            return {
                height: this.editorMode ? '100%' : `${this.chartHeight}px`,
                position: 'relative'
            }
        }
    },
    data() {
        return {
            chartData: { labels: [], datasets: [] } as IChartJSData,
            chartOptions: {} as IChartJSOptions,
            chartModel: {} as IChartJSChartModel,
            error: false,
            chartHeight: 0 as number
        }
    },
    watch: {
        dataToShow() {
            this.onRefreshChart()
        },
        editorMode() {
            this.onRefreshChart()
        }
    },
    mounted() {
        this.setEventListeners()
        this.onRefreshChart()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        ...mapActions(store, ['setSelections', 'getDatasetLabel']),
        setEventListeners() {
            emitter.on('refreshChart', this.onRefreshChart)
            emitter.on('chartWidgetResized', (newHeight) => this.onChartResize(newHeight as number))
        },
        removeEventListeners() {
            emitter.off('refreshChart', this.onRefreshChart)
            emitter.off('chartWidgetResized', (newHeight) => this.onChartResize(newHeight as number))
        },
        onRefreshChart(chartModel: any = null) {
            this.chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            this.updateChartModel()
        },
        updateChartModel() {
            if (!this.chartModel) {
                this.resetChart()
                return
            }
            this.updateChartOptions()
            this.updateChartData()
        },
        updateChartOptions() {
            this.chartOptions = { ...this.chartModel.options, responsive: true, maintainAspectRatio: false, events: ['click', 'mousemove'], onClick: this.setSelection }
        },
        updateChartData() {
            this.widgetModel.settings.chartModel.setData(this.dataToShow)
            this.widgetModel.settings.chartModel.updateChartColorSettings(this.widgetModel)
            this.chartData = this.chartModel.data
        },
        resetChart() {
            this.chartData = { labels: [], datasets: [] }
            this.chartOptions = {} as IChartJSOptions
        },
        setSelection(event: any, selectionEvent: any[]) {
            if (this.editorMode || !selectionEvent || !selectionEvent[0] || !this.widgetModel.settings.interactions.selection || !this.widgetModel.settings.interactions.selection.enabled) return
            const value = this.getSelectionValue(selectionEvent)
            updateStoreSelections(this.createNewSelection([value]), this.propActiveSelections, this.dashboardId, this.setSelections, this.$http)
        },
        getSelectionValue(selectionEvent: any[]) {
            const value = this.chartData.labels[selectionEvent[0].index]
            return value ?? ''
        },
        createNewSelection(value: (string | number)[]) {
            const attributeColumn = this.widgetModel.columns.find((column: IWidgetColumn) => column.fieldType === 'ATTRIBUTE')
            const selection = { datasetId: this.widgetModel.dataset as number, datasetLabel: this.getDatasetLabel(this.widgetModel.dataset as number), columnName: attributeColumn?.columnName ?? '', value: value, aggregated: false, timestamp: new Date().getTime() }
            return selection
        },
        onChartResize(newHeight: number) {
            this.chartHeight = newHeight
        }
    }
})
</script>
