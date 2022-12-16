import { HighchartsPieChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget'
import { KnowageHighcharts } from './KnowageHihgcharts'
import { createSerie, updatePieChartModel } from './updater/HighchartsPieChartUpdater'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartSerie, IHighchartsChartSerieData } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'

export class HighchartsPieChart extends KnowageHighcharts {
    constructor(model: any) {
        super()
        if (!this.model.plotOptions.pie) this.setPiePlotOptions()
        if (model && model.CHART) this.updateModel(model)
        else if (model) this.model = model
        console.log(">>>>>>>> LOADED MODEL: ", this.model)
        this.model.chart.type = "pie"
    }

    updateModel = (oldModel: any) => {
        updatePieChartModel(oldModel, this.model)
    }

    getModel = () => {
        return this.model
    }

    setModel = (model: HighchartsPieChartModel) => {
        this.model = model
    }

    setData = (data: any, widgetModel: IWidget, drillDownLevel = 0) => {
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

    getSeriesFromWidgetModel = (widgetModel: IWidget) => {
        const measureColumn = widgetModel.columns.find((column: IWidgetColumn) => column.fieldType === 'MEASURE')
        if (!measureColumn) return
        this.model.series = [createSerie(measureColumn.columnName, measureColumn.aggregation)]
    }

    setPiePlotOptions = () => {
        this.model.plotOptions.pie = highchartsDefaultValues.getDafaultPieChartPlotOptions()
    }


    updateSeriesLabelSettings = (widgetModel: IWidget) => {
        if (!widgetModel || !widgetModel.settings.series || !widgetModel.settings.series.seriesLabelsSettings || !widgetModel.settings.series.seriesLabelsSettings[0]) return
        const seriesLabelSetting = widgetModel.settings.series.seriesLabelsSettings[0]
        if (!seriesLabelSetting.label.enabled) return
        this.model.series.forEach((serie: IHighchartsChartSerie) => {
            serie.data.forEach((data: IHighchartsChartSerieData) => {
                data.dataLabels = {
                    backgroundColor: seriesLabelSetting.label.backgroundColor ?? '',
                    distance: 30,
                    enabled: true,
                    position: "",
                    style: {
                        fontFamily: seriesLabelSetting.label.style.fontFamily, fontSize: seriesLabelSetting.label.style.fontSize, fontWeight: seriesLabelSetting.label.style.fontWeight, color: seriesLabelSetting.label.style.color ?? ''
                    },
                    format: 'Test from claassss'  // TODO - Darko here comes the formatting
                }
            })

        })
    }
}
