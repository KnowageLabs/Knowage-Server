import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel, IHighchartsChartSerie, IHighchartsSerieAccessibility, IHighchartsSerieLabelSettings, ISerieAccessibilitySetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { createSerie, createGaugeSerie } from './updater/KnowageHighchartsCommonUpdater'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import Highcharts from 'highcharts'

export class KnowageHighcharts {
    model: IHighchartsChartModel
    cardinality: any[]
    range: any[]

    constructor() {
        this.model = this.createNewChartModel()
            ; (this.cardinality = []), (this.range = [])
    }

    async updateCardinality(data: any) {
        let cardinalityObj = {}
        this.model.settings.categories.forEach((category) => {
            let tempCategory = data.metaData.fields.filter((i) => i.header === category)
            if (tempCategory.length > 0) {
                cardinalityObj[tempCategory[0].name] = {
                    category: category,
                    set: new Set()
                }
            }
        })
        await data.rows.forEach((row: any, index: number) => {
            for (let k in cardinalityObj) {
                if (row[k]) cardinalityObj[k].set.add(row[k])
            }
        })
        this.cardinality = []
        for (let i in cardinalityObj) {
            this.cardinality.push({ [cardinalityObj[i].category]: cardinalityObj[i].set.size })
        }
        return this.cardinality
    }

    getModel() {
        return this.model
    }

    getCardinality() {
        return this.range
    }

    getRange() {
        return this.range
    }


    createNewChartModel() {
        return {
            title: '',
            lang: { noData: '' },
            chart: {
                options3d: highchartsDefaultValues.getDefault3DOptions(),
                type: ''
            },
            noData: highchartsDefaultValues.getDefaultNoDataConfiguration(),
            accessibility: highchartsDefaultValues.getDefaultAccessibilitySettings(),
            series: [],
            settings: {
                drilldown: {},
                categories: []
            },
            plotOptions: {
                series: { events: {} }
            },
            legend: highchartsDefaultValues.getDefaultLegendSettings(),
            tooltip: highchartsDefaultValues.getDefaultTooltipSettings(),
            colors: [],
            credits: { enabled: false }
        }
    }

    addSerie(column: IWidgetColumn, serieType: 'pie' | 'gauge') {
        switch (serieType) {
            case 'pie':
                this.model.series.push(createSerie(column.columnName, column.aggregation, true))
                break
            case 'gauge':
                this.model.series.push(createGaugeSerie(column.columnName))
                break
        }
    }

    removeSerie(column: IWidgetColumn) {
        const index = this.model.series.findIndex((tempSerie: IHighchartsChartSerie) => tempSerie.name === column.columnName)
        if (index !== -1) this.model.series.splice(index, 1)
    }

    updateSeriesAccessibilitySettings(widgetModel: IWidget) {
        if (!widgetModel || !widgetModel.settings.accesssibility || !widgetModel.settings.accesssibility.seriesAccesibilitySettings) return
        this.setAllSeriesAccessibilitySettings(widgetModel)
        this.setSpecificAccessibilitySettings(widgetModel)
    }

    setAllSeriesAccessibilitySettings(widgetModel: IWidget) {
        this.model.series.forEach((serie: IHighchartsChartSerie) => {
            if (this.model.chart.type !== 'pie' && this.model.chart.type !== 'solidgauge' && widgetModel.settings.accesssibility.seriesAccesibilitySettings[0] && widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility.enabled) {
                serie.accessibility = {
                    ...widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility
                }
            } else {
                serie.accessibility = highchartsDefaultValues.getDefaultSerieAccessibilitySetting()
            }
        })
    }

    setSpecificAccessibilitySettings(widgetModel: IWidget) {
        const index = this.model.chart.type !== 'pie' ? 1 : 0
        for (let i = index; i < widgetModel.settings.accesssibility.seriesAccesibilitySettings.length; i++) {
            const seriesAccesibilitySetting = widgetModel.settings.accesssibility.seriesAccesibilitySettings[i] as ISerieAccessibilitySetting
            if (seriesAccesibilitySetting.accessibility.enabled) seriesAccesibilitySetting.names.forEach((serieName: string) => this.updateSerieAccessibilitySettings(serieName, seriesAccesibilitySetting.accessibility))
        }
    }

    updateSerieAccessibilitySettings(serieName: string, accessibility: IHighchartsSerieAccessibility) {
        const index = this.model.series.findIndex((serie: IHighchartsChartSerie) => serie.name === serieName)
        if (index !== -1) this.model.series[index].accessibility = { ...accessibility }
    }

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
    }

    updateLegendSettings() {
        if (this.model.plotOptions.pie) this.model.plotOptions.pie.showInLegend = true
        return this.updateFormatterSettings(this.model.legend, 'labelFormat', 'labelFormatter', 'labelFormatterText', 'labelFormatterError')
    }

    updateTooltipSettings() {
        let hasError = this.updateFormatterSettings(this.model.tooltip, null, 'formatter', 'formatterText', 'formatterError')
        if (hasError) return hasError
        hasError = this.updateFormatterSettings(this.model.tooltip, null, 'pointFormatter', 'pointFormatterText', 'pointFormatterError')
        return hasError
    }

    updateChartColorSettings(widgetModel: IWidget) {
        if (!this.model.plotOptions || !this.model.chart.type) return
        this.model.colors = widgetModel.settings.chart.colors
    }

    handleFormatter(that: any, seriesLabelSetting: IHighchartsSerieLabelSettings) {
        const prefix = seriesLabelSetting.prefix
        const suffix = seriesLabelSetting.suffix
        const precision = seriesLabelSetting.precision
        const decimalPoints = Highcharts.getOptions().lang?.decimalPoint
        const thousandsSep = Highcharts.getOptions().lang?.thousandsSep

        const showAbsolute = seriesLabelSetting.absolute
        const absoluteValue = showAbsolute ? this.createSeriesLabelFromParams(seriesLabelSetting.scale, Math.abs(that.y), precision, decimalPoints, thousandsSep) : ''

        const showPercentage = seriesLabelSetting.percentage
        const percentValue = showPercentage ? this.createPercentageValue(that.point.percentage, precision, decimalPoints, thousandsSep) : ''

        const rawValue = !showAbsolute && !showPercentage ? this.createSeriesLabelFromParams(seriesLabelSetting.scale, that.y, precision, decimalPoints, thousandsSep) : ''

        const showBrackets = showAbsolute && showPercentage

        return `${prefix}${rawValue}${absoluteValue} ${showBrackets ? `(${percentValue})` : `${percentValue}`}${suffix}`
    }

    createSeriesLabelFromParams(scaleFactor: string, value: number, precision: number, decimalPoints: string | undefined, thousandsSep: string | undefined) {
        switch (scaleFactor?.toUpperCase()) {
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

    createPercentageValue(value: number, precision: number, decimalPoints: string | undefined, thousandsSep: string | undefined) {
        return `${Highcharts.numberFormat(value, precision, decimalPoints, thousandsSep)}%`
    }
}
