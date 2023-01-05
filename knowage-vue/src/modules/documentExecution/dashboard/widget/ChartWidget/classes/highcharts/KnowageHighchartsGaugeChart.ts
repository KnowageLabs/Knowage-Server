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
        const chartColors = widgetModel.settings.chart.colors
        console.log(">>>>>>>>> CHART COLORS", chartColors)
        this.setAllSeriesSettings(widgetModel, chartColors)
        this.setSpecificSeriesSettings(widgetModel, chartColors)
    }

    setAllSeriesSettings(widgetModel: IWidget, chartColors: string[]) {
        const allSeriesSettings = widgetModel.settings.series.seriesLabelsSettings[0]
        if (allSeriesSettings.label.enabled) {
            this.model.series.forEach((serie: IHighchartsGaugeSerie, index: number) => this.updateSeriesDataWithSerieSettings(serie, allSeriesSettings, index, chartColors))
        } else {
            this.resetSeriesSettings(chartColors)
        }
    }

    resetSeriesSettings(chartColors: string[]) {
        this.model.series.forEach((serie: IHighchartsGaugeSerie, index: number) => {
            const color = chartColors[index % chartColors.length] ?? ''
            serie.data.forEach((data: IHighchartsGaugeSerieData) => {
                data.dataLabels = { ...highchartsDefaultValues.getDefaultSerieLabelSettings(), position: '' }
                data.dataLabels.formatter = undefined
            })
            if (serie.dial) {
                serie.dial = highchartsDefaultValues.getDefaultSerieDialSettings()
                serie.dial.backgroundColor = color

            }
            if (serie.pivot) {
                serie.pivot = highchartsDefaultValues.getDefaultSeriePivotSettings()
                serie.pivot.backgroundColor = color
            }
        })
    }

    setSpecificSeriesSettings(widgetModel: IWidget, chartColors: string[]) {
        for (let i = 1; i < widgetModel.settings.series.seriesLabelsSettings.length; i++) {
            const seriesSettings = widgetModel.settings.series.seriesLabelsSettings[i] as IHighchartsSeriesLabelsSetting
            if (seriesSettings.label.enabled) seriesSettings.names.forEach((serieName: string) => this.updateSpecificSeriesLabelSettings(serieName, seriesSettings, chartColors))
        }
    }

    updateSpecificSeriesLabelSettings(serieName: string, seriesSettings: IHighchartsSeriesLabelsSetting, chartColors: string[]) {
        const index = this.model.series.findIndex((serie: IHighchartsGaugeSerie) => serie.name === serieName)
        if (index !== -1) this.updateSeriesDataWithSerieSettings(this.model.series[index], seriesSettings, index, chartColors)
    }

    updateSeriesDataWithSerieSettings(serie: IHighchartsGaugeSerie, seriesSettings: IHighchartsSeriesLabelsSetting, index: number, chartColors: string[]) {
        const color = chartColors[index % chartColors.length] ?? ''
        serie.data.forEach((data: IHighchartsGaugeSerieData) => {
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
        })
        if (seriesSettings.dial) serie.dial = seriesSettings.dial
        if (seriesSettings.pivot) serie.pivot = seriesSettings.pivot
        if (serie.dial && !serie.dial.backgroundColor) serie.dial.backgroundColor = color
        if (serie.pivot && !serie.pivot.backgroundColor) serie.pivot.backgroundColor = color
    }
}
