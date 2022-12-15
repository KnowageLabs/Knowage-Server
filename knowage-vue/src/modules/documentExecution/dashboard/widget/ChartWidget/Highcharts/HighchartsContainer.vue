<template>
    <div v-show="!error" id="container" style="width: 100%; height: 400px"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget } from '../../../Dashboard'
import { IHighchartsChartModel } from '../../../interfaces/highcharts/DashboardHighchartsWidget'
import Highcharts from 'highcharts'
import Highcharts3D from 'highcharts/highcharts-3d'
import Accessibility from 'highcharts/modules/accessibility'
import NoDataToDisplay from 'highcharts/modules/no-data-to-display'
import SeriesLabel from 'highcharts/modules/series-label'

Accessibility(Highcharts)
NoDataToDisplay(Highcharts)
SeriesLabel(Highcharts)
Highcharts3D(Highcharts)

export default defineComponent({
    name: 'highcharts-container',
    components: {},
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        dataToShow: { type: Object as any, required: true },
        dashboardId: { type: String, required: true },
        editorMode: { type: Boolean }
    },
    data() {
        return {
            chartModel: {} as IHighchartsChartModel,
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
            emitter.on('chartWidgetResized', (widget) => this.onRefreshChart())
        },
        removeEventListeners() {
            emitter.off('refreshChart', this.onRefreshChart)
            emitter.on('chartWidgetResized', (widget) => this.onRefreshChart())
        },
        onRefreshChart() {
            this.chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            this.widgetModel.settings.chartModel.model = this.chartModel
            this.updateChartModel()
        },
        updateChartModel() {
            Highcharts.setOptions({
                lang: {
                    noData: this.chartModel.lang.noData
                }
            })

            this.widgetModel.settings.chartModel.setData(this.dataToShow, this.widgetModel)

            this.widgetModel.settings.chartModel.updateSeriesAccessibilitySettings(this.widgetModel)
            this.widgetModel.settings.chartModel.updateSeriesLabelSettings(this.widgetModel)
            this.error = this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.plotOptions.pie?.dataLabels, 'format', 'formatter', 'formatterText', 'formatterError')
            if (this.error) return
            this.error = this.updateLegendSettings()
            if (this.error) return
            this.error = this.updateTooltipSettings()
            if (this.error) return

            this.widgetModel.settings.chartModel.updateChartColorSettings(this.widgetModel)

            //TODO - Remove Hardcoded
            this.chartModel.series = [
                {
                    type: 'pie',
                    name: 'Share',
                    // dataLabels: { enabled: true, format: '{point.percentage}' },
                    data: [
                        {
                            name: 'Xiaomi',
                            y: 12,
                            sliced: true,
                            selected: true
                        },
                        {
                            name: 'Samsung',
                            y: 10,
                            sliced: true,
                            selected: true
                        },
                        {
                            name: 'Motorola',
                            y: 5,
                            sliced: true,
                            selected: true
                        }
                    ]
                }
            ] as any[]

            console.log('>>>>>>>>>>>>>>> CHART TO RENDER: ', this.chartModel)
            Highcharts.chart('container', this.chartModel as any)
        },
        updateLegendSettings() {
            if (this.chartModel.plotOptions.pie) this.chartModel.plotOptions.pie.showInLegend = true
            return this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.legend, 'labelFormat', 'labelFormatter', 'labelFormatterText', 'labelFormatterError')
        },
        updateTooltipSettings() {
            let hasError = this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.tooltip, null, 'formatter', 'formatterText', 'formatterError')
            if (hasError) return hasError
            hasError = this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.tooltip, null, 'pointFormatter', 'pointFormatterText', 'pointFormatterError')
            return hasError
        }
    }
})
</script>
