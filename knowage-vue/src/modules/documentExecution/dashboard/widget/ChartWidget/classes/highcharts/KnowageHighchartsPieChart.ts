import { KnowageHighcharts } from './KnowageHihgcharts'
import { updatePieChartModel } from './updater/KnowageHighchartsPieChartUpdater'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel, IHighchartsChartSerie, IHighchartsChartSerieData } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { createSerie } from './updater/KnowageHighchartsCommonUpdater'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import Highcharts from 'highcharts'
import deepcopy from 'deepcopy'

export class KnowageHighchartsPieChart extends KnowageHighcharts {
    constructor(model: any) {
        super()
        if (!this.model.plotOptions.pie || model.chart.type !== 'pie') this.setPiePlotOptions()
        if (model && model.CHART) this.updateModel(deepcopy(model))
        else if (model) this.model = deepcopy(model)
        this.model.chart.type = 'pie'
    }

    updateModel(oldModel: any) {
        updatePieChartModel(oldModel, this.model)
    }

    setModel(model: IHighchartsChartModel) {
        this.model = model
    }

    setData(data: any, widgetModel: IWidget) {
        if (this.model.series.length === 0) this.getSeriesFromWidgetModel(widgetModel)

        this.model.series.map((item, serieIndex) => {
            this.range[serieIndex] = { serie: item.name }
            item.data = []
            data?.rows?.forEach((row: any, index: number) => {
                let serieElement = {
                    id: row.id,
                    name: row['column_1'],
                    y: row['column_2'],
                    drilldown: false
                }
                this.range[serieIndex].min = this.range[serieIndex].min ? Math.min(this.range[serieIndex].min, row['column_2']) : row['column_2']
                this.range[serieIndex].max = this.range[serieIndex].max ? Math.max(this.range[serieIndex].max, row['column_2']) : row['column_2']
                if (this.model.settings.drilldown) serieElement.drilldown = true
                item.data.push(serieElement)
            })
        })
        return this.model.series
    }

    getSeriesFromWidgetModel(widgetModel: IWidget) {
        const measureColumn = widgetModel.columns.find((column: IWidgetColumn) => column.fieldType === 'MEASURE')
        if (!measureColumn) return
        this.model.series = [createSerie(measureColumn.columnName, measureColumn.aggregation, true)]
    }

    setPiePlotOptions() {
        this.model.plotOptions.pie = highchartsDefaultValues.getDafaultPieChartPlotOptions()
    }

    updateSeriesLabelSettings(widgetModel: IWidget) {
        if (!widgetModel || !widgetModel.settings.series || !widgetModel.settings.series.seriesLabelsSettings || !widgetModel.settings.series.seriesLabelsSettings[0]) return
        const seriesLabelSetting = widgetModel.settings.series.seriesLabelsSettings[0]
        if (!seriesLabelSetting.label.enabled) return
        this.model.series.forEach((serie: IHighchartsChartSerie) => {
            serie.data.forEach((data: IHighchartsChartSerieData) => {
                data.dataLabels = {
                    backgroundColor: seriesLabelSetting.label.backgroundColor ?? '',
                    distance: 30,
                    enabled: true,
                    position: '',
                    style: {
                        fontFamily: seriesLabelSetting.label.style.fontFamily,
                        fontSize: seriesLabelSetting.label.style.fontSize,
                        fontWeight: seriesLabelSetting.label.style.fontWeight,
                        color: seriesLabelSetting.label.style.color ?? ''
                    },
                    formatter: function () {
                        return KnowageHighchartsPieChart.prototype.handleFormatter(this, seriesLabelSetting.label)
                    }
                }
            })
        })
    }
}
