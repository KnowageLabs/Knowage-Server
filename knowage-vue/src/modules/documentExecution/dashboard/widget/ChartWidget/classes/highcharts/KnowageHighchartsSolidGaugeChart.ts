import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartSerieData, IHighchartsSeriesLabelsSetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { updateSolidGaugeChartModel } from './updater/KnowageHighchartsSolidGaugeChartUpdater'
import { IHighchartsGaugeSerie } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget'
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
}
