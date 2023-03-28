import { IWidget } from './../../../../Dashboard.d';
import { KnowageVegaChart } from './KnowageVegaChart';
import deepcopy from 'deepcopy'

export class KnowageVegaChartWordcloud extends KnowageVegaChart {
    constructor(model: any) {
        super()
        if (model) this.model = deepcopy(model)
        this.model.chart.type = 'wordcloud'
    }

    setData(data: any, widgetModel: IWidget) {
        if (!this.model.data[0]) return

        this.model.data[0].values = []
        let maxNumberOfWords = widgetModel.settings.configuration.textConfiguration.maxNumberOfWords ?? 100
        if (data && data.rows) {
            if (maxNumberOfWords > data.rows.length) maxNumberOfWords = data.rows.length
            for (let i = 0; i < maxNumberOfWords; i++) {
                this.model.data[0].values.push({ text: data.rows[i]['column_1'], count: data.rows[i]['column_2'] })
            }
        }
        return this.model.data
    }
}
