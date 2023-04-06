import { IHighchartsHeatmapAxis } from './../../../../../interfaces/highcharts/DashboardHighchartsHeatmapWidget.d';
import { hexToRgba } from '@/modules/documentExecution/dashboard/helpers/FormattingHelpers';
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries } from './KnowageHighchartsCommonUpdater'
import * as highchartsDefaultValues from '../../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'

export const updateHeatmapChartModel = (oldModel: any, newModel: IHighchartsChartModel) => {
    console.log('-------- OLD CHART MODEL: ', oldModel)
    getFormattedNoDataConfiguration(oldModel, newModel)
    getFormattedLegend(oldModel, newModel)
    getFormattedAxisSettings(oldModel, newModel, 'x')
    getFormattedAxisSettings(oldModel, newModel, 'y')
    //getFormattedSeries(oldModel, newModel, 1)
    getFormattedTooltipSettings(oldModel, newModel)
    console.log('-------- NEW CHART MODEL: ', newModel)

    return newModel
}

const getFormattedAxisSettings = (oldModel: any, newModel: IHighchartsChartModel, axis: 'x' | 'y') => {
    const oldAxis = axis === 'x' ? oldModel.CHART.AXES_LIST.AXIS[1] : oldModel.CHART.AXES_LIST.AXIS[0]
    const newModelAxis = highchartsDefaultValues.getDefaultHeatmapXAxis()
    if (!oldAxis) return
    setFormattedAxisLabels(oldAxis, newModelAxis)
    setFormattedAxisTitle(oldAxis, newModelAxis)
    axis === 'x' ? newModel.xAxis = newModelAxis : newModel.yAxis = newModelAxis
}

const setFormattedAxisLabels = (oldAxis: any, newModelAxis: IHighchartsHeatmapAxis) => {
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
}

const setFormattedAxisTitle = (oldAxis: any, newModelAxis: IHighchartsHeatmapAxis) => {
    const oldAxisTitle = oldAxis.TITLE
    if (!oldAxisTitle) return
    if (oldAxisTitle.text) {
        newModelAxis.title.enabled = true
        newModelAxis.title.text = oldAxisTitle.text
    }
    if (oldAxisTitle.style) {
        if (oldAxisTitle.style.align) newModelAxis.title.align = getFormattedTitleAlign(oldAxis.style.align)
        if (oldAxisTitle.style.color) newModelAxis.title.style.color = hexToRgba(oldAxis.style.color)
        if (oldAxisTitle.style.fontFamily) newModelAxis.title.style.fontFamily = oldAxis.style.fontFamily
        if (oldAxisTitle.style.fontSize) newModelAxis.title.style.fontSize = oldAxis.style.fontSize
        if (oldAxisTitle.style.fontWeight) newModelAxis.title.style.fontWeight = oldAxis.style.fontWeight
    }
}

const getFormattedTitleAlign = (oldAxisTitleAlign: 'left' | 'center' | 'right') => {
    switch (oldAxisTitleAlign) {
        case 'left':
            return 'low'
        case 'right':
            return 'high'
        default:
            return 'center'
    }
}

const getFormattedTooltipSettings = (oldModel: any, newModel: IHighchartsChartModel) => {
    const oldTooltipSettings = oldModel.CHART?.TOOLTIP
    if (!oldTooltipSettings) return
    newModel.tooltip = {
        enabled: true,
        style: {
            fontFamily: oldTooltipSettings.style.fontFamily,
            fontSize: oldTooltipSettings.style.fontSize,
            fontWeight: oldTooltipSettings.style.fontWeight,
            color: oldTooltipSettings.style.color ? hexToRgba(oldTooltipSettings.style.color) : ''
        },
        backgroundColor: oldTooltipSettings.backgroundColor ? hexToRgba(oldTooltipSettings.backgroundColor) : ''
    }

}