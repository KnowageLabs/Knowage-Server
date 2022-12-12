<template>
    <div v-show="!error" id="container" style="width: 100%; height: 400px"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget, ISelection } from '../../../Dashboard'
import { IHighchartsChartModel, IHighchartsChartSerie, IHighchartsSerieAccessibility, IHighchartsSerieLabel, IHighchartsSerieLabelSettings, IHighchartsSeriesLabelsSetting, ISerieAccessibilitySetting } from '../../../interfaces/highcharts/DashboardHighchartsWidget'
import Highcharts from 'highcharts'
import Highcharts3D from 'highcharts/highcharts-3d'
import Accessibility from 'highcharts/modules/accessibility'
import NoDataToDisplay from 'highcharts/modules/no-data-to-display'
import SeriesLabel from 'highcharts/modules/series-label'
import deepcopy from 'deepcopy'

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
        this.updateChartModel()
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
            this.updateChartModel()
        },
        updateChartModel() {
            // Create the chart
            Highcharts.setOptions({
                lang: {
                    noData: this.chartModel.lang.noData
                }
            })

            this.widgetModel.settings.chartModel.setData(this.dataToShow)

            this.updateSeriesAccessibilitySettings()
            this.updateSeriesLabelSettings()
            this.error = this.updateFormatterSettings(this.chartModel.plotOptions.pie?.dataLabels, 'format', 'formatter', 'formatterText', 'formatterError')
            if (this.error) return
            this.error = this.updateLegendSettings()
            if (this.error) return
            this.error = this.updateTooltipSettings()
            if (this.error) return

            // // TODO - Remove Hardcoded
            // this.chartModel.series = [
            //     {
            //         type: 'pie',
            //         name: 'Share',
            //         dataLabels: { enabled: true },
            //         label: {
            //             enabled: true,
            //             style: {
            //                 fontFamily: '',
            //                 fontSize: '',
            //                 fontWeight: '',
            //                 color: '',
            //                 backgroundColor: ''
            //             },
            //             format: 'Prefix + {name} + Suffix'
            //         },
            //         data: [
            //             {
            //                 name: 'Xiaomi',
            //                 y: 12,
            //                 sliced: true,
            //                 selected: true
            //             }
            //         ]
            //     }
            // ]
            console.log('>>>>>>>>>>>>>>> CHART TO RENDER: ', this.chartModel)
            Highcharts.chart('container', this.chartModel as any)
        },
        updateSeriesAccessibilitySettings() {
            if (!this.widgetModel || !this.widgetModel.settings.accesssibility || !this.widgetModel.settings.accesssibility.seriesAccesibilitySettings) return
            this.setAllSeriesAccessibilitySettings()
            this.setSpecificAccessibilitySettings()
        },
        setAllSeriesAccessibilitySettings() {
            this.chartModel.series.forEach((serie: IHighchartsChartSerie) => {
                if (this.chartModel.chart.type !== 'pie' && this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[0] && this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility.enabled) {
                    serie.accessibility = {
                        ...this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility
                    }
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
        updateSeriesLabelSettings() {
            if (!this.widgetModel || !this.widgetModel.settings.series || !this.widgetModel.settings.series.seriesLabelsSettings) return
            this.setAllSeriesLabelSettings()
            this.setSpecificLabelSettings()
        },
        setAllSeriesLabelSettings() {
            this.chartModel.series.forEach((serie: IHighchartsChartSerie) => {
                if (this.chartModel.chart.type !== 'pie' && this.widgetModel.settings.series.seriesLabelsSettings[0] && this.widgetModel.settings.series.seriesLabelsSettings[0].label.enabled) {
                    serie.label = {
                        ...this.widgetModel.settings.series.seriesLabelsSettings
                    } // TODO
                } else {
                    serie.label = {
                        enabled: true,
                        style: {
                            fontFamily: '',
                            fontSize: '',
                            fontWeight: '',
                            color: '',
                            backgroundColor: ''
                        },
                        format: 'Prefix + {name} + Suffix'
                    }
                }
            })
        },
        setSpecificLabelSettings() {
            // const index = this.chartModel.chart.type !== 'pie' ? 1 : 0
            const index = 0
            for (let i = index; i < this.widgetModel.settings.series.seriesLabelsSettings.length; i++) {
                const seriesLabelSetting = this.widgetModel.settings.series.seriesLabelsSettings[i] as IHighchartsSeriesLabelsSetting
                if (seriesLabelSetting.label.enabled) seriesLabelSetting.names.forEach((serieName: string) => this.updateSerieLabelSettings(serieName, seriesLabelSetting.label))
            }
        },
        updateSerieLabelSettings(serieName: string, label: IHighchartsSerieLabelSettings) {
            const index = this.chartModel.series.findIndex((serie: IHighchartsChartSerie) => serie.name === serieName)
            if (index !== -1) {
                // TODO
            }
        },
        updateFormatterSettings(object: any, formatProperty: string | null, formatterProperty: string, formatterTextProperty: string, formatterErrorProperty: string) {
            let hasError = false
            if (formatProperty && object[formatProperty]?.trim() === '') delete object[formatProperty]
            if (!object[formatterTextProperty] || !object[formatterTextProperty].trim()) {
                delete object[formatterProperty]
                object[formatterErrorProperty] = ''
                return hasError
            } else {
                try {
                    const fn = eval(`(${object[formatterTextProperty]})`)
                    if (typeof fn === 'function') object[formatterProperty] = fn
                    object[formatterErrorProperty] = ''
                } catch (error) {
                    object[formatterErrorProperty] = (error as any).message
                    hasError = true
                }
            }

            return hasError
        },
        updateLegendSettings() {
            if (this.chartModel.plotOptions.pie) this.chartModel.plotOptions.pie.showInLegend = true
            return this.updateFormatterSettings(this.chartModel.legend, 'labelFormat', 'labelFormatter', 'labelFormatterText', 'labelFormatterError')
        },
        updateTooltipSettings() {
            let hasError = this.updateFormatterSettings(this.chartModel.tooltip, null, 'formatter', 'formatterText', 'formatterError')
            if (hasError) return hasError
            hasError = this.updateFormatterSettings(this.chartModel.tooltip, null, 'pointFormatter', 'pointFormatterText', 'pointFormatterError')
            return hasError
        }
    }
})
</script>
