import { IWidget } from "../../../Dashboard"
import { hexToRgba } from "../../../helpers/FormattingHelpers"
import { IHighchartsGaugeSerieData } from "../../../interfaces/highcharts/DashboardHighchartsGaugeWidget"
import { IHighchartsChartModel } from "../../../interfaces/highcharts/DashboardHighchartsWidget"
import { getRGBColorFromString } from '../../WidgetEditor/helpers/WidgetEditorHelpers'
import Highcharts from 'highcharts'

export const formatActivityGauge = (formattedChartModel: IHighchartsChartModel, widgetModel: IWidget) => {
    formattedChartModel.chart.type = 'solidgauge'
    const colors = widgetModel.settings.chart.colors
    formattedChartModel.pane.background = []
    for (let i = 0; i < formattedChartModel.series.length; i++) {
        let serieData = formattedChartModel.series[i].data[0] as IHighchartsGaugeSerieData
        if (!serieData) continue
        const temp = {
            outerRadius: serieData.radius,
            innerRadius: serieData.innerRadius,
            backgroundColor: serieData.color !== '' ? serieData.color : colors[i],
            borderWidth: 0
        }
        temp.backgroundColor = reduceOpacityFromColorString(temp.backgroundColor, 0.3)
        formattedChartModel.pane.background.push(temp)
    }

}

const reduceOpacityFromColorString = (colorString: string | null, newOpacity: number) => {
    if (!colorString) return null
    const color = colorString.startsWith('#') ? hexToRgba(colorString) : colorString
    const rgbaColor = getRGBColorFromString(color)
    if (rgbaColor.a) rgbaColor.a = newOpacity
    return rgbaColor ? `rgba(${rgbaColor.r}, ${rgbaColor.g}, ${rgbaColor.b}, ${rgbaColor.a})` : 'none'
}

export const formatHeatmap = (formattedChartModel: IHighchartsChartModel) => {
    const tooltip = formattedChartModel.tooltip as any
    tooltip.formatter = function (this: Highcharts.TooltipFormatterContextObject) {
        return this.point.options.value ? this.series.name + '<br/><b>' + this.point.options.id + ': </b>' + tooltip.valuePrefix + this.point.options.value + tooltip.valueSuffix : this.series.name;
    }
}