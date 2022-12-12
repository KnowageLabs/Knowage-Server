import { IChartJSDefaultLegendSettings, IChartJSDefaultTooltipSettings } from "@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget"
import deepcopy from "deepcopy"
import descriptor from './ChartJSDefaultValuesDescriptor.json'

export const getDefaultTooltipSettings = () => {
    return deepcopy(descriptor.defaultTooltipSettings) as IChartJSDefaultTooltipSettings
}


export const getDefaultLegendSettings = () => {
    return deepcopy(descriptor.defaultLegendSettings) as IChartJSDefaultLegendSettings
}
