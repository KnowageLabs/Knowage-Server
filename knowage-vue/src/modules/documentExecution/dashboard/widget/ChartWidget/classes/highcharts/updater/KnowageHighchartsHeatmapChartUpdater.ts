import { hexToRgba } from '@/modules/documentExecution/dashboard/helpers/FormattingHelpers';
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries, getFormattedTooltipSettings } from './KnowageHighchartsCommonUpdater'
import * as highchartsDefaultValues from '../../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'

export const updateHeatmapChartModel = (oldModel: any, newModel: IHighchartsChartModel) => {
    console.log('-------- OLD CHART MODEL: ', oldModel)
    getFormattedNoDataConfiguration(oldModel, newModel)
    getFormattedLegend(oldModel, newModel)
    getFormattedAxisLabels(oldModel, newModel, 'x')
    getFormattedAxisLabels(oldModel, newModel, 'y')
    //getFormattedSeries(oldModel, newModel, 1)
    //getFormattedTooltipSettings(oldModel, newModel)
    console.log('-------- NEW CHART MODEL: ', newModel)

    return newModel
}

const getFormattedAxisLabels = (oldModel: any, newModel: IHighchartsChartModel, axis: 'x' | 'y') => {
    const oldAxis = axis === 'x' ? oldModel.CHART.AXES_LIST.AXIS[1] : oldModel.CHART.AXES_LIST.AXIS[0]
    const newModelAxis = highchartsDefaultValues.getDefaultHeatmapXAxis()
    if (!oldAxis) return
    console.log('-------- OLD AXIS: ', oldAxis)
    // TODO - Ask ON PEER if we need this 
    // if (oldAxis.min) newModelAxis.min = +oldAxis.min
    // if (oldAxis.max) newModelAxis.max = +oldAxis.max
    if (oldAxis.position) newModelAxis.labels.align = oldAxis.position
    if (oldAxis.style) {
        if (oldAxis.style.align) newModelAxis.labels.align = oldAxis.style.align
        if (oldAxis.style.color) newModelAxis.labels.style.color = hexToRgba(oldAxis.style.color)
        if (oldAxis.style.fontFamily) newModelAxis.labels.style.fontFamily = oldAxis.style.fontFamily
        if (oldAxis.style.fontSize) newModelAxis.labels.style.fontSize = oldAxis.style.fontSize
        if (oldAxis.style.fontWeight) newModelAxis.labels.style.fontWeight = oldAxis.style.fontWeight
        if (oldAxis.style.rotate) newModelAxis.labels.rotation = oldAxis.style.rotate
    }
    axis === 'x' ? newModel.xAxis = newModelAxis : newModel.yAxis = newModelAxis
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