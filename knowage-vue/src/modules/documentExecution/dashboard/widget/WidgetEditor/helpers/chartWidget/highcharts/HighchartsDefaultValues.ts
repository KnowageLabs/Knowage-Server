import { IWidgetSelection } from "@/modules/documentExecution/dashboard/Dashboard"
import { IHighchartsAccessibilitySettings, IHighchartsLegend, IHighchartsNoDataConfiguration, IHighchartsOptions3D, IIHighchartsPieChartPlotOptions, IHighchartsSeriesLabelsSetting, IHighchartsTooltip, ISerieAccessibilitySetting } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
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
    return deepcopy(descriptor.defaultPieChartPlotOptions) as IIHighchartsPieChartPlotOptions
}

export const getDefaultHighchartsSelections = () => {
    return deepcopy(descriptor.defaultHighchartsSelection) as IWidgetSelection
}

export const getDefaultSeriesAccessibilitySettings = () => {
    return deepcopy(descriptor.defaultSeriesAccessibilitySettings) as ISerieAccessibilitySetting
}

export const getDefaultAllSeriesAccessibilitySettings = () => {
    return deepcopy(descriptor.defaultAllSeriesAccessibilitySettings) as ISerieAccessibilitySetting[]
}


export const getDefaultSerieLabelSettings = () => {
    return deepcopy(descriptor.defaultSerieLabelSettings) as IHighchartsSeriesLabelsSetting[]
}