import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsGaugeSerie, IHighchartsGaugeSerieData } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget'
import { KnowageHighcharts } from './KnowageHihgcharts'
import { createGaugeSerie } from './updater/KnowageHighchartsCommonUpdater'

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
                    y: row[`column_${i + 1}`],
                } as IHighchartsGaugeSerieData
                if (maxNumberOfSeries === 4) {
                    serieElement.radius = startingRadius + '%',
                        serieElement.innerRadius = startingInnerRadius + '%',
                        startingRadius -= 25
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

}
