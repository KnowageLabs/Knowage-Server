import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsSeriesLabelsSetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { updateSolidGaugeChartModel } from './updater/KnowageHighchartsSolidGaugeChartUpdater'
import { IHighchartsGaugeSerie, IHighchartsGaugeSerieData } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget'
import { KnowageHighchartsGaugeChart } from './KnowageHighchartsGaugeChart'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import deepcopy from 'deepcopy'

export class KnowageHighchartsSolidGaugeChart extends KnowageHighchartsGaugeChart {
    constructor(model: any) {
        super()
        console.log(">>>>>>>>>>>> KnowageHighchartsSolidGaugeChart called with: ", deepcopy(model))
        this.setSpecificOptionsDefaultValues()
        if (model && model.CHART) this.updateModel(deepcopy(model))
        else if (model) {
            this.model = deepcopy(model)
            if (model.chart.type !== 'solidgauge') {
                this.setSpecificOptionsDefaultValues()
            }
        }
        this.model.chart.type = 'solidgauge'
    }

    updateModel(oldModel: any) {
        updateSolidGaugeChartModel(oldModel, this.model)
    }

    setData(data: any, widgetModel: IWidget) {
        this.setGaugeData(data, widgetModel, 1)
    }

    setSpecificOptionsDefaultValues() {
        this.setGaugePlotOptions()
        this.setGaugePaneSettings()
        this.setGaugeYAxis()
    }

    setGaugePlotOptions() {
        this.model.plotOptions.solidgauge = highchartsDefaultValues.getDafaultGaugeChartPlotOptions()
    }

    setGaugePaneSettings() {
        this.model.pane = highchartsDefaultValues.getDafaultSolidGaugePaneOptions()
    }

    setGaugeYAxis() {
        this.model.yAxis = highchartsDefaultValues.getDefaultGaugeYAxis()
        this.model.yAxis.tickWidth = 0
    }

    updateSeriesLabelSettings(widgetModel: IWidget) {
        if (!widgetModel || !widgetModel.settings.series || !widgetModel.settings.series.seriesLabelsSettings) return
        const seriesSettings = widgetModel.settings.series.seriesLabelsSettings[0]
        this.updateSeriesDataWithSerieSettings(this.model.series[0], seriesSettings)
    }


    updateSeriesDataWithSerieSettings(serie: IHighchartsGaugeSerie, seriesSettings: IHighchartsSeriesLabelsSetting) {
        if (!serie || !seriesSettings) return
        console.log(">>>>>>>>>>>> SERIES SETTING: ", seriesSettings)
        serie.data.forEach((data: IHighchartsGaugeSerieData) => {
            data.dataLabels = {
                backgroundColor: null,
                distance: 30,
                enabled: seriesSettings.label.enabled,
                position: '',
                style: {
                    fontFamily: seriesSettings.label.style.fontFamily,
                    fontSize: seriesSettings.label.style.fontSize,
                    fontWeight: seriesSettings.label.style.fontWeight,
                    color: seriesSettings.label.style.color ?? ''
                },
                formatter: function () {
                    return KnowageHighchartsGaugeChart.prototype.handleFormatter(this, seriesSettings.label)
                }
            }
        })
        if (seriesSettings.label.enabled) {

        }

    }
}
