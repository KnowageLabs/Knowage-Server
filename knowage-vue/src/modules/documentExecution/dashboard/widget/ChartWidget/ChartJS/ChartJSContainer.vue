<template>
    <Pie :chart-options="chartOptions" :chart-data="chartData" :chart-id="'pie-chart'" :dataset-id-key="'label'" :width="200" :height="200" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale } from 'chart.js'
import { IWidget, IDataset, IWidgetColumn, ISelection } from '../../../Dashboard'
import { IChartJSChartModel, IChartJSData, IChartJSOptions } from '../../../interfaces/chartJS/DashboardChartJSWidget'
import { mapActions } from 'pinia'
import { updateStoreSelections } from '../../interactionsHelpers/InteractionHelper'
import store from '../../../Dashboard.store'

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale)

export default defineComponent({
    name: 'chartJS-container',
    components: { Pie },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, dataToShow: { type: Object as any, required: true }, dashboardId: { type: String, required: true }, editorMode: { type: Boolean }, propActiveSelections: { type: Array as PropType<ISelection[]>, required: true } },
    data() {
        return {
            chartData: { labels: [], datasets: [] } as IChartJSData,
            chartOptions: {} as IChartJSOptions,
            chartModel: {} as IChartJSChartModel,
            error: false
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
        ...mapActions(store, ['setSelections', 'getAllDatasets']),
        setEventListeners() {
            emitter.on('refreshChart', this.onRefreshChart)
        },
        removeEventListeners() {
            emitter.off('refreshChart', this.onRefreshChart)
        },
        onRefreshChart() {
            this.chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            this.widgetModel.settings.chartModel.model = this.chartModel
            this.updateChartModel()
        },
        updateChartModel() {
            if (!this.chartModel) {
                this.resetChart()
                return
            }
            this.updateChartOptions()
            this.updateChartData()
            console.log('>>>>>>>>> UPDATE CHART MODEL: ', this.chartModel)
        },
        updateChartOptions() {
            // TODO see if responsive is needed
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
            if (this.editorMode || !selectionEvent || !selectionEvent[0]) return
            const value = this.getSelectionValue(selectionEvent)
            updateStoreSelections(this.createNewSelection([value]), this.propActiveSelections, this.dashboardId, this.setSelections, this.$http)
        },
        getSelectionValue(selectionEvent: any[]) {
            const value = this.chartData.datasets[selectionEvent[0].datasetIndex].data[selectionEvent[0].index]
            return value ?? ''
        },
        createNewSelection(value: (string | number)[]) {
            const measureColumn = this.widgetModel.columns.find((column: IWidgetColumn) => column.fieldType === 'MEASURE')
            const selection = { datasetId: this.widgetModel.dataset as number, datasetLabel: this.getDatasetLabel(this.widgetModel.dataset as number), columnName: measureColumn?.columnName ?? '', value: value, aggregated: false, timestamp: new Date().getTime() }
            return selection
        },
        getDatasetLabel(datasetId: number) {
            const datasets = this.getAllDatasets()
            const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
            return index !== -1 ? datasets[index].label : ''
        }
    }
})
</script>
