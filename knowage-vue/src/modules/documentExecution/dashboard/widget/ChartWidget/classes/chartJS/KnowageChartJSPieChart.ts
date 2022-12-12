import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard";
import { updatePieChartModel } from './updater/KnowageChartJSUpdater'
import { KnowageChartJS } from "./KnowageChartJS"

export class ChartJSPieChart extends KnowageChartJS {

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

    setData = (data: any) => {
        // this.model.data = {
        //     datasets: [{
        //         label: this.model.settings.dimensions[0],
        //         backgroundColor: [],
        //         data: []
        //     }],
        //     labels: []
        // }

        // const dimensionColumnName = data.metaData.fields.filter((i) => i.header === this.model.settings.dimensions[0])[0].name
        // const valuesColumnName = data.metaData.fields.filter((i) => i.header === `${this.model.settings.values[0].name}_${this.model.settings.values[0].aggregation}`)[0].name
        // data.rows.forEach((row, index) => {
        //     this.model.data.labels.push(row[dimensionColumnName])
        //     this.model.data.datasets[0].data.push(row[valuesColumnName])
        //     if (this.model.colors && this.model.colors[index]) {
        //         this.model.data.datasets[0].backgroundColor.push(this.model.colors[index])
        //     }
        // })
        // return this.model.data
    }
}
