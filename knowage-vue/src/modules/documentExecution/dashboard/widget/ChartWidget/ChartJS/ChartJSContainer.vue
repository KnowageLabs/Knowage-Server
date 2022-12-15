<template>
    <Pie :chart-options="chartOptions" :chart-data="chartData" :chart-id="'pie-chart'" :dataset-id-key="'label'" :width="200" :height="200" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale } from 'chart.js'
import { IWidget } from '../../../Dashboard'
import { IChartJSChartModel, IChartJSData, IChartJSOptions } from '../../../interfaces/chartJS/DashboardChartJSWidget'

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale)

export default defineComponent({
    name: 'chartJS-container',
    components: { Pie },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, dataToShow: { type: Object as any, required: true }, dashboardId: { type: String, required: true }, editorMode: { type: Boolean } },
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
            this.chartOptions = { ...this.chartModel.options, responsive: true, maintainAspectRatio: false }
        },
        updateChartData() {
            // TODO - Darko
            this.widgetModel.settings.chartModel.setData(this.dataToShow)
            this.chartData = this.chartModel.data

            //TODO - Find better place for color settings
            this.chartModel.data.datasets[0].backgroundColor = this.widgetModel.settings.chart.colors

            // TODO REMOVE MOCK
            // this.chartData = {
            //     labels: ['VueJs', 'EmberJs', 'ReactJs', 'AngularJs'],
            //     datasets: [
            //         {
            //             backgroundColor: ['#41B883', '#E46651', '#00D8FF', '#DD1B16'],
            //             data: [40, 20, 80, 10]
            //         }
            //     ]
            // }
        },
        resetChart() {
            this.chartData = { labels: [], datasets: [] }
            this.chartOptions = {} as IChartJSOptions
        }
    }
})
</script>
