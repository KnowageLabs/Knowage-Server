import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { HighchartsPieChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget'
import { KnowageHighcharts } from './KnowageHihgcharts'
import { updatePieChartModel } from './updater/HighchartsPieChartUpdater'

export class HighchartsPieChart extends KnowageHighcharts {
    constructor(model: any, widgetModel: IWidget) {
        super(model, widgetModel)
        if (model && model.CHART) this.updateModel(model, widgetModel)
        else this.model = model
    }

    updateModel = (oldModel: any, widgetModel: IWidget) => {
        updatePieChartModel(oldModel, this.model, widgetModel)
    }

    getModel = () => {
        return this.model
    }

    setModel = (model: HighchartsPieChartModel) => {
        this.model = model
    }

    setData = (data: any, drillDownLevel = 0) => {
        //hardcoding column values because we will always have one measure and one category, by hardcoding the values, we are saving resourcces on forEach and filter methods
        // const categoryColumnName = data.metaData.fields.filter((i) => i.header === this.model.settings.categories[drillDownLevel])[0].name

        this.model.series.map((item, serieIndex) => {
            // const dataColumn = item.groupingFunction ? item.name + '_' + item.groupingFunction : item.name
            this.range[serieIndex] = { serie: item.name }
            // const dataColumnName = data.metaData.fields.filter((i) => i.header === dataColumn)[0].name
            item.data = []
            data.rows.forEach((row: any, index: number) => {
                let serieElement = {
                    id: row.id,
                    name: row['column_1'], //hardcoded because category should always be the first one
                    y: row['column_2'], //measure should always be the second one row[dataColumnName]
                    drilldown: false,
                    color: '#' + (((1 << 24) * Math.random()) | 0).toString(16).padStart(6, '0')
                }
                // this.range[serieIndex].min = this.range[serieIndex].min ? Math.min(this.range[serieIndex].min, row[dataColumnName]) : row[dataColumnName]
                // this.range[serieIndex].max = this.range[serieIndex].max ? Math.max(this.range[serieIndex].max, row[dataColumnName]) : row[dataColumnName]
                this.range[serieIndex].min = this.range[serieIndex].min ? Math.min(this.range[serieIndex].min, row['column_2']) : row['column_2']
                this.range[serieIndex].max = this.range[serieIndex].max ? Math.max(this.range[serieIndex].max, row['column_2']) : row['column_2']
                if (this.model.settings.drilldown) serieElement.drilldown = true
                if (this.model.settings.colorPalette?.COLOR[index]?.value) serieElement.color = this.model.settings.colorPalette.COLOR[index].value
                item.data.push(serieElement)
            })
        })
        return this.model.series
    }
}
