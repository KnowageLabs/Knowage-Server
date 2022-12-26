import { KnowageHighcharts } from './KnowageHihgcharts'
import { updatePieChartModel } from './updater/KnowageHighchartsPieChartUpdater'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel, IHighchartsChartSerie, IHighchartsChartSerieData } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { createSerie } from './updater/KnowageHighchartsGaugeChartUpdater'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import Highcharts from 'highcharts'
import deepcopy from 'deepcopy'

export class KnowageHighchartsPieChart extends KnowageHighcharts {
    constructor(model: any) {
        super()
        if (!this.model.plotOptions.pie) this.setPiePlotOptions()
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
        //hardcoding column values because we will always have one measure and one category, by hardcoding the values, we are saving resourcces on forEach and filter methods
        // const categoryColumnName = data.metaData.fields.filter((i) => i.header === this.model.settings.categories[drillDownLevel])[0].name
        if (this.model.series.length === 0) this.getSeriesFromWidgetModel(widgetModel)

        this.model.series.map((item, serieIndex) => {
            // const dataColumn = item.groupingFunction ? item.name + '_' + item.groupingFunction : item.name
            this.range[serieIndex] = { serie: item.name }
            // const dataColumnName = data.metaData.fields.filter((i) => i.header === dataColumn)[0].name
            item.data = []
            data?.rows?.forEach((row: any, index: number) => {
                let serieElement = {
                    id: row.id,
                    name: row['column_1'], //hardcoded because category should always be the first one
                    y: row['column_2'], //measure should always be the second one row[dataColumnName]
                    drilldown: false
                }
                // this.range[serieIndex].min = this.range[serieIndex].min ? Math.min(this.range[serieIndex].min, row[dataColumnName]) : row[dataColumnName]
                // this.range[serieIndex].max = this.range[serieIndex].max ? Math.max(this.range[serieIndex].max, row[dataColumnName]) : row[dataColumnName]
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
        this.model.series = [createSerie(measureColumn.columnName, measureColumn.aggregation)]
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

    // TODO - Darko move to common file/reuse
    handleFormatter(that, seriesLabelSetting) {
        var prefix = seriesLabelSetting.prefix
        var suffix = seriesLabelSetting.suffix
        var precision = seriesLabelSetting.precision
        var decimalPoints = Highcharts.getOptions().lang?.decimalPoint
        var thousandsSep = Highcharts.getOptions().lang?.thousandsSep

        var absoluteValue = ''
        var showAbsolute = seriesLabelSetting.absolute
        if (showAbsolute) absoluteValue = this.createAbsoluteValue(seriesLabelSetting.scale, that.y, precision, decimalPoints, thousandsSep)

        var percentValue = ''
        var showPercentage = seriesLabelSetting.percentage
        if (showPercentage) var percentValue = this.createPercentageValue(that.point.percentage, precision, decimalPoints, thousandsSep)

        // var categoryName = '' //CR: is category name needed?
        // displayValue = categoryName

        var showBrackets = showAbsolute && showPercentage

        return `${prefix} ${absoluteValue} ${showBrackets ? `(${percentValue})` : `${percentValue}`}  ${suffix}`
    }

    // TODO - Darko move to common file/reuse
    createAbsoluteValue(scaleFactor, value, precision, decimalPoints, thousandsSep) {
        switch (scaleFactor.toUpperCase()) {
            case 'EMPTY':
                return Highcharts.numberFormat(value, precision, decimalPoints, thousandsSep)
            case 'K':
                return Highcharts.numberFormat(value / Math.pow(10, 3), precision, decimalPoints, thousandsSep) + 'k'
            case 'M':
                return Highcharts.numberFormat(value / Math.pow(10, 6), precision, decimalPoints, thousandsSep) + 'M'
            case 'G':
                return Highcharts.numberFormat(value / Math.pow(10, 9), precision, decimalPoints, thousandsSep) + 'G'
            case 'T':
                return Highcharts.numberFormat(value / Math.pow(10, 12), precision, decimalPoints, thousandsSep) + 'T'
            case 'P':
                return Highcharts.numberFormat(value / Math.pow(10, 15), precision, decimalPoints, thousandsSep) + 'P'
            case 'E':
                return Highcharts.numberFormat(value / Math.pow(10, 18), precision, decimalPoints, thousandsSep) + 'E'
            default:
                return Highcharts.numberFormat(value, precision, decimalPoints, thousandsSep)
        }
    }

    // TODO - Darko move to common file/reuse
    createPercentageValue(value, precision, decimalPoints, thousandsSep) {
        return `${Highcharts.numberFormat(value, precision, decimalPoints, thousandsSep)}%`
    }
}
