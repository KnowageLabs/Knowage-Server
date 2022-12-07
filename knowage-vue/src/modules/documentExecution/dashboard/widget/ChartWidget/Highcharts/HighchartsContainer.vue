<template>
    <button @click="updateChartModel">Test</button>
    {{ chartModel?.legend }}
    <div v-show="!error" id="container" style="width: 100%; height: 400px"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget, ISelection } from '../../../Dashboard'
import { IHighchartsChartSerie, IHighchartsSerieAccessibility, ISerieAccessibilitySetting } from '../../../interfaces/highcharts/DashboardHighchartsWidget'
import Highcharts from 'highcharts'
import Highcharts3D from 'highcharts/highcharts-3d'
import Accessibility from 'highcharts/modules/accessibility'
import NoDataToDisplay from 'highcharts/modules/no-data-to-display'
import deepcopy from 'deepcopy'

Accessibility(Highcharts)
NoDataToDisplay(Highcharts)
Highcharts3D(Highcharts)

export default defineComponent({
    name: 'highcharts-container',
    components: {},
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        dataToShow: { type: Object as any, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        dashboardId: { type: String, required: true },
        editorMode: { type: Boolean }
    },
    data() {
        return {
            chartModel: {} as any,
            error: false
        }
    },
    mounted() {
        this.setEventListeners()
        this.onRefreshChart()
        this.updateChartModel()
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
            this.updateChartModel()
            console.log('>>>>>>>>>> refreshChart: ', this.chartModel)
        },
        updateChartModel() {
            // TODO - remove this
            if (this.widgetModel.type !== 'chart') return

            // Create the chart
            Highcharts.setOptions({
                lang: {
                    noData: 'No data message'
                }
            })

            // TODO - Remove Hardcoded
            // this.chartModel.series = [
            //     {
            //         type: 'pie',
            //         name: 'Share',
            //         data: [
            //             ['Samsung', 23],
            //             ['Apple', 18],
            //             {
            //                 showInLegend: true,
            //                 name: 'Xiaomi',
            //                 y: 12,
            //                 sliced: true,
            //                 selected: true
            //             },
            //             ['Oppo*', 9],
            //             ['Vivo', 8],
            //             ['Others', 30]
            //         ]
            //     }
            // ]
            this.widgetModel.settings.chartModel.setData(this.dataToShow)

            this.updateSeriesAccessibilitySettings()
            this.error = this.updateLabelSettings()
            if (this.error) return

            this.chartModel.plotOptions.pie.showInLegend = true
            console.log('>>>>>>>>>>>> ABOUT TO RENDER CHART...', this.chartModel)
            Highcharts.chart('container', this.chartModel)
        },
        updateSeriesAccessibilitySettings() {
            if (!this.widgetModel || !this.widgetModel.settings.accesssibility || !this.widgetModel.settings.accesssibility.seriesAccesibilitySettings) return
            this.setAllSeriesAccessibilitySettings()
            this.setSpecificAccessibilitySettings()
        },
        setAllSeriesAccessibilitySettings() {
            this.chartModel.series.forEach((serie: IHighchartsChartSerie) => {
                if (this.chartModel.chart.type !== 'pie' && this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[0] && this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility.enabled) {
                    serie.accessibility = { ...this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility }
                } else {
                    serie.accessibility = {
                        enabled: false,
                        description: '',
                        exposeAsGroupOnly: false,
                        keyboardNavigation: { enabled: false }
                    }
                }
            })
        },
        setSpecificAccessibilitySettings() {
            const index = this.chartModel.chart.type !== 'pie' ? 1 : 0
            for (let i = index; i < this.widgetModel.settings.accesssibility.seriesAccesibilitySettings.length; i++) {
                const seriesAccesibilitySetting = this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[i] as ISerieAccessibilitySetting
                if (seriesAccesibilitySetting.accessibility.enabled) seriesAccesibilitySetting.names.forEach((serieName: string) => this.updateSerieAccessibilitySettings(serieName, seriesAccesibilitySetting.accessibility))
            }
        },
        updateSerieAccessibilitySettings(serieName: string, accessibility: IHighchartsSerieAccessibility) {
            const index = this.chartModel.series.findIndex((serie: IHighchartsChartSerie) => serie.name === serieName)
            if (index !== -1) this.chartModel.series[index].accessibility = { ...accessibility }
        },
        updateLabelSettings() {
            let hasError = false
            if (this.chartModel.plotOptions.pie.dataLabels.format?.trim() === '') delete this.chartModel.plotOptions.pie.dataLabels.format
            if (!this.chartModel.plotOptions.pie.dataLabels.formatterText) {
                delete this.chartModel.plotOptions.pie.dataLabels.formatter
                return hasError
            } else {
                try {
                    const fn = eval(`(${this.chartModel.plotOptions.pie.dataLabels.formatterText})`)
                    if (typeof fn === 'function') this.chartModel.plotOptions.pie.dataLabels.formatter = fn
                    this.chartModel.plotOptions.pie.dataLabels.formatterError = ''
                } catch (error) {
                    this.chartModel.plotOptions.pie.dataLabels.formatterError = (error as any).message
                    hasError = true
                }
            }

            return hasError
        }
    }
})
</script>
