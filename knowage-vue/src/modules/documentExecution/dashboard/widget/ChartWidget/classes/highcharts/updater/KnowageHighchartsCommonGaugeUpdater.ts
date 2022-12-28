import { hexToRgba } from "@/modules/documentExecution/dashboard/helpers/FormattingHelpers"
import { IHighchartsChartModel } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"


export const getFormattedPaneSettings = (oldModel: any, newModel: IHighchartsChartModel) => {
    if (oldModel.CHART.PANE) {
        newModel.pane = {
            startAngle: oldModel.CHART.PANE.startAngle,
            endAngle: oldModel.CHART.PANE.endAngle,
            center: newModel.pane.center
        }
    }
}

export const getFormattedScaleSettings = (oldModel: any, newModel: IHighchartsChartModel) => {
    const oldYAxis = oldModel.CHART.AXES_LIST.AXIS[0]
    newModel.yAxis.min = oldYAxis.min
    newModel.yAxis.max = oldYAxis.max
}

export const getFormattedTickSettings = (oldModel: any, newModel: IHighchartsChartModel) => {
    const oldYAxis = oldModel.CHART.AXES_LIST.AXIS[0]
    newModel.yAxis.tickPosition = oldYAxis.tickPosition
    newModel.yAxis.tickColor = oldYAxis.tickColor ? hexToRgba(oldYAxis.tickColor) : ''
    newModel.yAxis.tickLength = oldYAxis.tickLength
    newModel.yAxis.tickWidth = oldYAxis.tickWidth
    newModel.yAxis.minorTickInterval = oldYAxis.minorTickInterval
}

export const getFormattedBandsSettings = (oldModel: any, newModel: IHighchartsChartModel) => {
    const oldYAxis = oldModel.CHART.AXES_LIST.AXIS[0]
    newModel.yAxis.plotBands = []
    oldYAxis.PLOTBANDS?.PLOT?.forEach((plot: { from: number, to: number, color: string }) => newModel.yAxis.plotBands.push({ from: plot.from, to: plot.to, color: plot.color ? hexToRgba(plot.color) : '', thickness: 10 }))
}