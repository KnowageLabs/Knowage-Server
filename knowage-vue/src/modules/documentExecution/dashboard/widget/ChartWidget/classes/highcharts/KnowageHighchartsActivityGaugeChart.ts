import { KnowageHighchartsGaugeChart } from './KnowageHighchartsGaugeChart'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel, IHighchartsSeriesLabelsSetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { updateActivityGaugeChartModel } from './updater/KnowageHighchartsActivityGaugeChartUpdater'
import { IHighchartsGaugeActivityTooltip, IHighchartsGaugeSerie, IHighchartsGaugeSerieData } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import deepcopy from 'deepcopy'

export class KnowageHighchartsActivityGaugeChart extends KnowageHighchartsGaugeChart {
    constructor(model: any) {
        super()
        this.setSpecificOptionsDefaultValues()
        this.setTooltipSettings()
        if (model && model.CHART) this.updateModel(deepcopy(model))
        else if (model) {
            this.model = deepcopy(model)
            if (model.chart.type !== 'activitygauge') {
                this.formatSeriesFromOtherChartTypeSeries()
                this.setSpecificOptionsDefaultValues()
                this.setTooltipSettings()
            }
        }
        this.model.chart.type = 'activitygauge'
    }

    updateModel(oldModel: any) {
        updateActivityGaugeChartModel(oldModel, this.model)
    }

    setModel(model: IHighchartsChartModel) {
        this.model = model
    }

    setSpecificOptionsDefaultValues() {
        this.setGaugePlotOptions()
        this.setGaugePaneSettings()
        this.setGaugeYAxis()
    }

    setData(data: any, widgetModel: IWidget) {
        this.setGaugeData(data, widgetModel, 4)
    }

    setGaugePlotOptions() {
        this.model.plotOptions.solidgauge = highchartsDefaultValues.getdefaultActivityGaugeChartPlotOptions()
    }

    setGaugePaneSettings() {
        this.model.pane = highchartsDefaultValues.getDefaultActivityGaugePaneOptions()
    }

    setGaugeYAxis() {
        this.model.yAxis = highchartsDefaultValues.getDefaultActivityGaugeYAxis()
    }

    setTooltipSettings() {
        this.model.tooltip = highchartsDefaultValues.getDefaultActivityGaugeTooltip() as IHighchartsGaugeActivityTooltip
    }

    updateSeriesLabelSettings(widgetModel: IWidget) {
        if (!widgetModel || !widgetModel.settings.series || !widgetModel.settings.series.seriesLabelsSettings) return
        this.setAllSeriesSettings(widgetModel)
        this.setSpecificSeriesSettings(widgetModel)
    }

    setAllSeriesSettings(widgetModel: IWidget) {
        const allSeriesSettings = widgetModel.settings.series.seriesLabelsSettings[0]
        if (allSeriesSettings?.serieColorEnabled) {
            this.model.series.forEach((serie: IHighchartsGaugeSerie) => {
                this.updateSeriesDataWithSerieSettings(serie, allSeriesSettings)
            })
        } else {
            this.resetSeriesSettings()
        }
    }

    resetSeriesSettings() {
        this.model.series.forEach((serie: IHighchartsGaugeSerie) => {
            serie.data.forEach((data: IHighchartsGaugeSerieData) => {
                data.color = ''
                delete data.dataLabels
            })
        })
    }
    setSpecificSeriesSettings(widgetModel: IWidget) {
        for (let i = 1; i < widgetModel.settings.series.seriesLabelsSettings.length; i++) {
            const seriesSettings = widgetModel.settings.series.seriesLabelsSettings[i] as IHighchartsSeriesLabelsSetting
            if (seriesSettings.serieColorEnabled) seriesSettings.names.forEach((serieName: string) => this.updateSpecificSeriesSettings(serieName, seriesSettings))
        }
    }

    updateSpecificSeriesSettings(serieName: string, seriesSettings: IHighchartsSeriesLabelsSetting) {
        const index = this.model.series.findIndex((serie: IHighchartsGaugeSerie) => serie.name === serieName)
        if (index !== -1) this.updateSeriesDataWithSerieSettings(this.model.series[index], seriesSettings)
    }

    updateSeriesDataWithSerieSettings(serie: IHighchartsGaugeSerie, seriesSettings: IHighchartsSeriesLabelsSetting) {
        serie.data.forEach((data: IHighchartsGaugeSerieData) => {
            data.color = seriesSettings.serieColor ?? ''
            delete data.dataLabels
        })
    }
}
