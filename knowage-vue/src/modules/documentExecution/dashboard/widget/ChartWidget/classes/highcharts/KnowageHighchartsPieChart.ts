import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { HighchartsPieChartModel } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget"
import { KnowageHighcharts } from "./KnowageHihgcharts"
import { updatePieChartModel } from "./updater/HighchartsPieChartUpdater"

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
        return this.model;
    }

    setModel = (model: HighchartsPieChartModel) => {
        this.model = model
    }

    setData = (data: any, drillDownLevel = 0) => {
        console.log(">>>>>>>> setData callllllllllllllllllllllllleeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeed")
        const categoryColumnName = data.metaData.fields.filter((i) => i.header === this.model.settings.categories[drillDownLevel])[0].name
        this.model.series.map((item, serieIndex) => {
            const dataColumn = item.groupingFunction ? item.name + '_' + item.groupingFunction : item.name
            this.range[serieIndex] = { serie: item.name }
            const dataColumnName = data.metaData.fields.filter((i) => i.header === dataColumn)[0].name
            item.data = []
            data.rows.forEach((row: any, index: number) => {
                let serieElement = {
                    "id": row.id,
                    "name": row[categoryColumnName],
                    "y": row[dataColumnName],
                    drilldown: false,
                    color: '' as any
                }
                this.range[serieIndex].min = this.range[serieIndex].min ? Math.min(this.range[serieIndex].min, row[dataColumnName]) : row[dataColumnName]
                this.range[serieIndex].max = this.range[serieIndex].max ? Math.max(this.range[serieIndex].max, row[dataColumnName]) : row[dataColumnName]
                if (this.model.settings.drilldown) serieElement.drilldown = true
                if (this.model.settings.colorPalette?.COLOR[index]?.value) serieElement.color = this.model.settings.colorPalette.COLOR[index].value
                item.data.push(serieElement)
            })

        })
        return this.model.series
    }
}
