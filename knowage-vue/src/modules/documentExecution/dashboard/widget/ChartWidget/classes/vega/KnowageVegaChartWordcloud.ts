import { updateWordcloudChartModel } from './updater/KnowageVegaChartWordcloudUpdater';
import { KnowageVegaChart } from './KnowageVegaChart';
import deepcopy from 'deepcopy'

export class KnowageVegaChartWordcloud extends KnowageVegaChart {
    constructor(model: any) {
        super()
        if (model && model.CHART) this.updateModel(deepcopy(model))
        else if (model) this.model = deepcopy(model)
        this.model.chart.type = 'wordcloud'
    }

    updateModel(oldModel: any) {
        updateWordcloudChartModel(oldModel, this.model)
    }

    setData(data: any) {
        console.log("------- DATA: ", data)
        if (!this.model.data[0]) return
        this.model.data[0].values = []
        if (data && data.rows) {
            data.rows.forEach((row: any) => {
                this.model.data[0].values.push({ text: row['column_1'], count: row['column_2'] })
            })
        }

        return this.model.data
    }
}
