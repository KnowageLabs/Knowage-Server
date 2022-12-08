import { IHighchartsAccessibilitySettings, IHighchartsLegend, IHighchartsNoDataConfiguration, IHighchartsOptions3D, IHighchartsPieChartPlotOptions, IHighchartsTooltip } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import deepcopy from "deepcopy"
import descriptor from './HighchartsDefaultValuesDescriptor.json'

export const getDefaultNoDataConfiguration = () => {
    return deepcopy(descriptor.defaultNoDataConfiguration) as IHighchartsNoDataConfiguration
}

export const getDefaultAccessibilitySettings = () => {
    return deepcopy(descriptor.defaultAccessibilitySettings) as IHighchartsAccessibilitySettings
}

export const getDefault3DOptions = () => {
    return deepcopy(descriptor.default3DOptions) as IHighchartsOptions3D
}

export const getDefaultTooltipSettings = () => {
    return deepcopy(descriptor.defaultTooltipSettings) as IHighchartsTooltip
}


export const getDefaultLegendSettings = () => {
    return deepcopy(descriptor.defaultLegendSettings) as IHighchartsLegend
}

export const getDafaultPieChartPlotOptions = () => {
    return deepcopy(descriptor.defaultPieChartPlotOptions) as IHighchartsPieChartPlotOptions
}

