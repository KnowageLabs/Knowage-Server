import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartSerieData, IHighchartsSeriesLabelsSetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { updateSolidGaugeChartModel } from './updater/KnowageHighchartsSolidGaugeChartUpdater'
import { IHighchartsGaugeSerie, IHighchartsGaugeSerieData } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget'
import { KnowageHighchartsGaugeChart } from './KnowageHighchartsGaugeChart'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import Highcharts from 'highcharts'
import deepcopy from 'deepcopy'

export class KnowageHighchartsSolidGaugeChart extends KnowageHighchartsGaugeChart {
    constructor(model: any) {
        super()
        if (!this.model.plotOptions.solidgauge || model.chart.type !== 'solidgauge') this.setGaugePlotOptions()
        if (!this.model.pane || model.chart.type !== 'solidgauge') this.setGaugePaneSettings()
        if (!this.model.yAxis || model.chart.type !== 'solidgauge') this.setGaugeYAxis()
        if (model && model.CHART) this.updateModel(deepcopy(model))
        else if (model) this.model = deepcopy(model)
        this.model.chart.type = 'solidgauge'
    }

    updateModel(oldModel: any) {
        updateSolidGaugeChartModel(oldModel, this.model)
    }

    setData(data: any, widgetModel: IWidget) {
        this.setGaugeData(data, widgetModel, 1)
    }

    setGaugePlotOptions() {
        this.model.plotOptions.solidgauge = highchartsDefaultValues.getDafaultGaugeChartPlotOptions()
    }

    setGaugePaneSettings() {
        this.model.pane = highchartsDefaultValues.getDafaultSolidGaugePaneOptions()
    }

    setGaugeYAxis() {
        this.model.yAxis = highchartsDefaultValues.getDefaultGaugeYAxis()
    }

    updateSeriesLabelSettings(widgetModel: IWidget) {
        if (!widgetModel || !widgetModel.settings.series || !widgetModel.settings.series.seriesLabelsSettings) return
        const chartColors = widgetModel.settings.chart.colors
        this.setAllSeriesSettings(widgetModel, chartColors)
        this.setSpecificSeriesSettings(widgetModel, chartColors)
    }

    setAllSeriesSettings(widgetModel: IWidget, chartColors: string[]) {
        const allSeriesSettings = widgetModel.settings.series.seriesLabelsSettings[0]
        this.resetSeriesSettings(chartColors)
        this.model.series.forEach((serie: IHighchartsGaugeSerie, index: number) => {
            const color = chartColors[index % chartColors.length] ?? ''
            this.updateSeriesDataWithSerieSettings(serie, allSeriesSettings, index, color)
        })

    }

    resetSeriesSettings(chartColors: string[]) {
        this.model.series.forEach((serie: IHighchartsGaugeSerie, index: number) => {
            const color = chartColors[index % chartColors.length] ?? ''
            serie.data.forEach((data: IHighchartsGaugeSerieData) => {
                if (this.model.chart.type === 'activitygauge') {
                    data.color = ''
                    delete data.dataLabels
                }
                else {
                    data.dataLabels = { ...highchartsDefaultValues.getDefaultSerieLabelSettings(), position: '' }
                    data.dataLabels.formatter = undefined
                }
            })
            this.resetDialAndPivotSerieSettings(serie, color)
        })
    }

    resetDialAndPivotSerieSettings(serie: IHighchartsGaugeSerie, color: string) {
        if (serie.dial) {
            serie.dial = highchartsDefaultValues.getDefaultSerieDialSettings()
            serie.dial.backgroundColor = color
        }
        if (serie.pivot) {
            serie.pivot = highchartsDefaultValues.getDefaultSeriePivotSettings()
            serie.pivot.backgroundColor = color
        }
    }

    setSpecificSeriesSettings(widgetModel: IWidget, chartColors: string[]) {
        for (let i = 1; i < widgetModel.settings.series.seriesLabelsSettings.length; i++) {
            const seriesSettings = widgetModel.settings.series.seriesLabelsSettings[i] as IHighchartsSeriesLabelsSetting
            seriesSettings.names.forEach((serieName: string) => this.updateSpecificSeriesLabelSettings(serieName, seriesSettings, chartColors))
        }
    }

    updateSpecificSeriesLabelSettings(serieName: string, seriesSettings: IHighchartsSeriesLabelsSetting, chartColors: string[]) {
        const index = this.model.series.findIndex((serie: IHighchartsGaugeSerie) => serie.name === serieName)
        if (index !== -1) {
            const color = chartColors[index % chartColors.length] ?? ''
            this.updateSeriesDataWithSerieSettings(this.model.series[index], seriesSettings, index, color)
        }
    }

    updateSeriesDataWithSerieSettings(serie: IHighchartsGaugeSerie, seriesSettings: IHighchartsSeriesLabelsSetting, index: number, color: string) {
        console.log(">>>>>>>>>>>> SERIES SETTING: ", seriesSettings)
        serie.data.forEach((data: IHighchartsGaugeSerieData) => {
            if (this.model.chart.type === 'activitygauge') {
                delete data.dataLabels
                data.color = seriesSettings.serieColorEnabled ? seriesSettings.serieColor : ''
            }
            else if (seriesSettings.label.enabled) {
                data.dataLabels = {
                    y: index * 40,
                    backgroundColor: seriesSettings.label.backgroundColor ?? color,
                    distance: 30,
                    enabled: true,
                    position: '',
                    style: {
                        fontFamily: seriesSettings.label.style.fontFamily,
                        fontSize: seriesSettings.label.style.fontSize,
                        fontWeight: seriesSettings.label.style.fontWeight,
                        color: seriesSettings.label.style.color ?? color
                    },
                    formatter: function () {
                        return KnowageHighchartsGaugeChart.prototype.handleFormatter(this, seriesSettings.label)
                    }
                }
            }
        })
        if (this.model.chart.type !== 'activitygauge') this.updateSeriesDialAndPivotSettings(serie, seriesSettings, color)
    }

    updateSeriesDialAndPivotSettings(serie: IHighchartsGaugeSerie, seriesSettings: IHighchartsSeriesLabelsSetting, color: string) {
        if (seriesSettings.dial) {
            serie.dial = { ...seriesSettings.dial }
            if (!seriesSettings.dial.backgroundColor) serie.dial.backgroundColor = color
        }
        if (seriesSettings.pivot) {
            serie.pivot = { ...seriesSettings.pivot }
            if (!seriesSettings.pivot.backgroundColor) serie.pivot.backgroundColor = color
        }
    }
}
