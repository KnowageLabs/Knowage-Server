import { KnowageHighchartsGaugeChart } from './KnowageHighchartsGaugeChart'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel, IHighchartsChartSerieData, IHighchartsSeriesLabelsSetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { updateActivityGaugeChartModel } from './updater/KnowageHighchartsActivityGaugeChartUpdater'
import { IHighchartsGaugeSerie } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import Highcharts from 'highcharts'
import deepcopy from 'deepcopy'

export class KnowageHighchartsActivityGaugeChart extends KnowageHighchartsGaugeChart {
    constructor(model: any) {
        super()
        if (!this.model.plotOptions.solidgauge || model.chart.type !== 'activitygauge') this.setGaugePlotOptions()
        if (!this.model.pane || model.chart.type !== 'activitygauge') this.setGaugePaneSettings()
        if (!this.model.yAxis || model.chart.type !== 'activitygauge') this.setGaugeYAxis()
        if (model && model.CHART) this.updateModel(deepcopy(model))
        else if (model) this.model = deepcopy(model)
        this.model.chart.type = 'activitygauge'
    }

    updateModel(oldModel: any) {
        updateActivityGaugeChartModel(oldModel, this.model)
    }


    setModel(model: IHighchartsChartModel) {
        this.model = model
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
}
