import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries, getFormattedTooltipSettings } from './KnowageHighchartsCommonUpdater'
import * as highchartsDefaultValues from '../../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'

export const updateHeatmapChartModel = (oldModel: any, newModel: IHighchartsChartModel) => {
    console.log('-------- OLD CHART MODEL: ', oldModel)
    getFormattedNoDataConfiguration(oldModel, newModel)
    getFormattedLegend(oldModel, newModel)
    getFormattedLabels(oldModel, newModel)
    //getFormattedSeries(oldModel, newModel, 1)
    //getFormattedTooltipSettings(oldModel, newModel)
    console.log('-------- NEW CHART MODEL: ', newModel)

    return newModel
}

const getFormattedLabels = (oldModel: any, newModel: IHighchartsChartModel) => {
    const oldYAxis = oldModel.CHART.AXES_LIST.AXIS[0]
    console.log('---------OLD Y AXIS: ', oldYAxis)
    const newModelYAxis = highchartsDefaultValues.getDefaultHeatmapXAxis()
    if (!oldYAxis) return
    if (oldYAxis.min) newModelYAxis.min = oldYAxis.min
    if (oldYAxis.max) newModelYAxis.max = oldYAxis.max
    if (oldYAxis.position) newModelYAxis.labels.align = oldYAxis.position
    if (oldYAxis.max) newModelYAxis.max = oldYAxis.max
    if (oldYAxis.max) newModelYAxis.max = oldYAxis.max
    if (oldYAxis.max) newModelYAxis.max = oldYAxis.max
    if (oldYAxis.max) newModelYAxis.max = oldYAxis.max
    if (oldYAxis.max) newModelYAxis.max = oldYAxis.max
    if (oldYAxis.max) newModelYAxis.max = oldYAxis.max


}



// export interface IHighchartsHeatmapAxis {
//     min: number | null,
//     max: number | null,
//     categories: string[],
//     labels: IHighchartsHeatmapAxisLabels
//     title: IHighchartsHeatmapAxisTitle
// }

// export interface IHighchartsHeatmapAxisLabels {
//     rotation: number | null
//     align: string,
//     style: {
//         fontFamily: string
//         fontSize: string
//         fontWeight: string
//         color: string
//     },
//     format?: string,
//     formatter?: Function,
//     formatterText?: string,
//     formatterError?: string,
// }

// export interface IHighchartsHeatmapAxisTitle {
//     enabled: boolean,
//     text: string,
//     style: {
//         fontFamily: string
//         fontSize: string
//         fontWeight: string
//         color: string
//     }
// }

// export interface IHighchartsHeatmapSerie {
//     name: string,
//     data: IHighchartsHeatmapSerieData[],
//     accessibility?: IHighchartsSerieAccessibility
// }

// export interface IHighchartsHeatmapSerieData {
//     id: string,
//     x: number,
//     y: number,
//     value: number,
//     dataLabels: IHighchartsChartDataLabels
// }