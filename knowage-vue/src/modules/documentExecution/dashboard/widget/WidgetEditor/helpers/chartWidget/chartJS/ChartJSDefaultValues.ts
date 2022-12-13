import { IWidgetSelection } from "@/modules/documentExecution/dashboard/Dashboard"
import { IChartJSLegendSettings, IChartJSTooltipSettings } from "@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget"
import deepcopy from "deepcopy"
import descriptor from './ChartJSDefaultValuesDescriptor.json'

export const getDefaultTooltipSettings = () => {
    return deepcopy(descriptor.defaultTooltipSettings) as IChartJSTooltipSettings
}

export const getDefaultLegendSettings = () => {
    return deepcopy(descriptor.defaultLegendSettings) as IChartJSLegendSettings
}

export const getDefaultChartJSSelections = () => {
    return deepcopy(descriptor.defaultChartJSSelection) as IWidgetSelection
}