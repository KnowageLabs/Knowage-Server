import { KnowageHighchartsGaugeChart } from './KnowageHighchartsGaugeChart'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { updateGaugeChartModel } from './updater/KnowageHighchartsGaugeChartUpdater'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import deepcopy from 'deepcopy'


export class KnowageHighchartsGaugeSeriesChart extends KnowageHighchartsGaugeChart {
    constructor(model: any) {
        super()
        if (!this.model.plotOptions.gauge || model.chart.type !== 'gauge') this.setGaugePlotOptions()
        if (!this.model.pane || model.chart.type !== 'gauge') this.setGaugePaneSettings()
        if (!this.model.yAxis || model.chart.type !== 'gauge') this.setGaugeYAxis()
        if (model && model.CHART) this.updateModel(deepcopy(model))
        else if (model) this.model = deepcopy(model)
        this.model.chart.type = 'gauge'
    }

    updateModel(oldModel: any) {
        updateGaugeChartModel(oldModel, this.model)
    }
    setData(data: any, widgetModel: IWidget) {
        this.setGaugeData(data, widgetModel, undefined)
    }

    setGaugePlotOptions() {
        this.model.plotOptions.gauge = highchartsDefaultValues.getDafaultGaugeChartPlotOptions()
    }

    setGaugePaneSettings() {
        this.model.pane = highchartsDefaultValues.getDafaultPaneOptions()
    }

    setGaugeYAxis() {
        this.model.yAxis = highchartsDefaultValues.getDefaultGaugeYAxis()
    }
}
