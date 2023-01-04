import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsGaugeSerie, IHighchartsGaugeSerieData } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget'
import { KnowageHighcharts } from './KnowageHihgcharts'
import { createGaugeSerie } from './updater/KnowageHighchartsCommonUpdater'
import { IHighchartsSerieLabelSettings, IHighchartsSeriesLabelsSetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import Highcharts from 'highcharts'

export class KnowageHighchartsGaugeChart extends KnowageHighcharts {
    constructor() {
        super()
    }

    setGaugeData(data: any, widgetModel: IWidget, maxNumberOfSeries: number | undefined) {
        this.getSeriesFromWidgetModel(widgetModel, maxNumberOfSeries)

        let startingRadius = 112
        let startingInnerRadius = 88

        for (let i = 0; i < this.model.series.length; i++) {
            const serie = this.model.series[i]
            serie.data = []
            data?.rows?.forEach((row: any) => {
                let serieElement = {
                    name: serie.name,
                    y: row[`column_${i + 1}`]
                } as IHighchartsGaugeSerieData
                if (maxNumberOfSeries === 4) {
                    ; (serieElement.radius = startingRadius + '%'), (serieElement.innerRadius = startingInnerRadius + '%'), (startingRadius -= 25)
                    startingInnerRadius -= 25
                }
                serie.data.push(serieElement)
            })
        }
        return this.model.series
    }

    getSeriesFromWidgetModel(widgetModel: IWidget, maxNumberOfSeries: number | undefined) {
        const newSeries = [] as IHighchartsGaugeSerie[]
        let seriesAdded = 0

        for (let i = 0; i < widgetModel.columns.length; i++) {
            this.addSerieFromExistingSeriesOrWidgetColumns(widgetModel.columns[i], newSeries)
            seriesAdded++
            if (seriesAdded === maxNumberOfSeries) break
        }
        this.model.series = newSeries
    }

    addSerieFromExistingSeriesOrWidgetColumns(column: IWidgetColumn, newSeries: IHighchartsGaugeSerie[]) {
        if (column.fieldType === 'MEASURE') {
            const index = this.model.series.findIndex((serie: IHighchartsGaugeSerie) => serie.name === column.columnName)
            index !== -1 ? newSeries.push(this.model.series[index]) : newSeries.push(createGaugeSerie(column.columnName))
        }
    }

    // TODO - Darko/Bojan move to superclass???
    updateSeriesLabelSettings(widgetModel: IWidget) {
        if (!widgetModel || !widgetModel.settings.series || !widgetModel.settings.series.seriesLabelsSettings) return
        this.setAllSeriesSettings(widgetModel)
        // this.setSpecificSeriesSettings(widgetModel)
    }

    setAllSeriesSettings(widgetModel: IWidget) {
        const allSeriesSettings = widgetModel.settings.series.seriesLabelsSettings[0]
        if (allSeriesSettings.label.enabled) {
            this.model.series.forEach((serie: IHighchartsGaugeSerie, index: number) => this.updateSeriesDataWithSerieSettings(serie, allSeriesSettings, index))
        } else {
            this.resetSeriesSettings()
        }
    }

    resetSeriesSettings() {
        this.model.series.forEach((serie: IHighchartsGaugeSerie) => {
            serie.data.forEach((data: IHighchartsGaugeSerieData) => {
                data.dataLabels = { ...highchartsDefaultValues.getDefaultSerieLabelSettings(), position: '' }
                data.dataLabels.formatter = undefined
            })
            if (serie.dial) serie.dial = highchartsDefaultValues.getDefaultSerieDialSettings()
            if (serie.pivot) serie.pivot = highchartsDefaultValues.getDefaultSeriePivotSettings()
        })
    }

    setSpecificSeriesSettings(widgetModel: IWidget) {
        for (let i = 1; i < widgetModel.settings.series.seriesLabelsSettings.length; i++) {
            const seriesSettings = widgetModel.settings.series.seriesLabelsSettings[i] as IHighchartsSeriesLabelsSetting
            console.log(">>>>>>>>>> seriesSettings: ", seriesSettings)
            if (seriesSettings.label.enabled) seriesSettings.names.forEach((serieName: string) => this.updateSpecificSeriesLabelSettings(serieName, seriesSettings))
        }
    }

    updateSpecificSeriesLabelSettings(serieName: string, seriesSettings: IHighchartsSeriesLabelsSetting) {
        const index = this.model.series.findIndex((serie: IHighchartsGaugeSerie) => serie.name === serieName)
        console.log(">>>>>>>>>> INDEX: ", index)
        if (index !== -1) this.updateSeriesDataWithSerieSettings(this.model.series[index], seriesSettings, index)
    }

    updateSeriesDataWithSerieSettings(serie: IHighchartsGaugeSerie, seriesSettings: IHighchartsSeriesLabelsSetting, index: number) {
        serie.data.forEach((data: IHighchartsGaugeSerieData) => {
            data.dataLabels = {
                y: index * 40,
                backgroundColor: seriesSettings.label.backgroundColor ?? '',
                distance: 30,
                enabled: true,
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
        if (seriesSettings.dial) serie.dial = seriesSettings.dial
        if (seriesSettings.pivot) serie.pivot = seriesSettings.pivot
    }

    // TODO - Darko move to common file/reuse
    handleFormatter(that: any, seriesLabelSetting: any) {
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

        // const categoryName = '' //CR: is category name needed?
        // displayValue = categoryName

        const showBrackets = showAbsolute && showPercentage

        return `${prefix}${rawValue}${absoluteValue} ${showBrackets ? `(${percentValue})` : `${percentValue}`}${suffix}`
    }

    // TODO - Darko move to common file/reuse
    createSeriesLabelFromParams(scaleFactor, value, precision, decimalPoints, thousandsSep) {
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

    // TODO - Darko move to common file/reuse
    createPercentageValue(value, precision, decimalPoints, thousandsSep) {
        return `${Highcharts.numberFormat(value, precision, decimalPoints, thousandsSep)}%`
    }
}
