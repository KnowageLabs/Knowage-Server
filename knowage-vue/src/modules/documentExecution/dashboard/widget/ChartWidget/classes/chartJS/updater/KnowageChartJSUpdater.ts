import { hexToRgb } from "@/modules/documentExecution/dashboard/helpers/FormattingHelpers"
import { IChartJSChartModel } from "@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget"

export const updatePieChartModel = (oldModel: any, newModel: IChartJSChartModel) => {
    getFormattedLegend(oldModel, newModel)
    getFormattedTooltipSettings(oldModel, newModel)

    return newModel
}

const getFormattedLegend = (oldModel: any, newModel: IChartJSChartModel) => {
    if (oldModel.CHART.LEGEND) {
        newModel.options.plugins.legend = {
            display: oldModel.CHART.LEGEND.show,
            position: oldModel.CHART.LEGEND.position,
            align: 'center',
        }
    }
}

const getFormattedTooltipSettings = (oldModel: any, newModel: IChartJSChartModel) => {
    if (oldModel.CHART.VALUES.SERIE && oldModel.CHART.VALUES.SERIE[0] && oldModel.CHART.VALUES.SERIE[0].TOOLTIP) {
        const oldTooltipSettings = oldModel.CHART.VALUES.SERIE[0].TOOLTIP
        newModel.options.plugins.tooltip = {
            enabled: true,
            bodyColor: oldTooltipSettings.style.color ? hexToRgb(oldTooltipSettings.style.color) : '',
            bodyFont: {
                family: oldTooltipSettings.style.fontFamily,
                size: oldTooltipSettings.style.fontSize ? oldTooltipSettings.style.fontSize.substring(0, oldTooltipSettings.style.fontSize.lastIndexOf('p')) : 0,
                style: '',
                weight: oldTooltipSettings.style.fontWeight,
            },
            backgroundColor: oldTooltipSettings.backgroundColor ? hexToRgb(oldTooltipSettings.backgroundColor) : '',
            bodyAlign: oldTooltipSettings.style.align ?? 'center'

        }
    }
}
