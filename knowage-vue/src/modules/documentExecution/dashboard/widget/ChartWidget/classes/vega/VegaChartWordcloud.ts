import { updateWordcloudChartModel } from './updater/VegaChartWordcloudUpdater';
import { VegaChart } from './VegaChart';
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import * as vegaChartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/vega/VegaDefaultValues'
import deepcopy from 'deepcopy'

export class VegaChartsWordcloud extends VegaChart {
    constructor(model: any) {
        super()
        if (model && model.CHART) this.updateModel(deepcopy(model))
        else if (model) this.model = deepcopy(model)


        // this.model.chart.type = 'wordcloud'
    }

    updateModel(oldModel: any) {
        updateWordcloudChartModel(oldModel, this.model)
    }

    setData(data: any, widgetModel: IWidget) {
        // if (this.model.series.length === 0) this.getSeriesFromWidgetModel(widgetModel)

        // this.model.series.map((item, serieIndex) => {
        //     this.range[serieIndex] = { serie: item.name }
        //     item.data = []
        //     data?.rows?.forEach((row: any) => {
        //         let serieElement = {
        //             id: row.id,
        //             name: row['column_1'],
        //             y: row['column_2'],
        //             drilldown: false
        //         }
        //         this.range[serieIndex].min = this.range[serieIndex].min ? Math.min(this.range[serieIndex].min, row['column_2']) : row['column_2']
        //         this.range[serieIndex].max = this.range[serieIndex].max ? Math.max(this.range[serieIndex].max, row['column_2']) : row['column_2']
        //         if (this.model.settings.drilldown) serieElement.drilldown = true
        //         item.data.push(serieElement)
        //     })
        // })
        // return this.model.series
    }

    getSeriesFromWidgetModel(widgetModel: IWidget) {
        const measureColumn = widgetModel.columns.find((column: IWidgetColumn) => column.fieldType === 'MEASURE')
        if (!measureColumn) return
        // this.model.series = [createSerie(measureColumn.columnName, measureColumn.aggregation, true)]
    }
}
