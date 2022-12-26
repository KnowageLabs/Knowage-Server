import { IWidgetSelection } from "@/modules/documentExecution/dashboard/Dashboard"
import { IHighchartsAccessibilitySettings, IHighchartsChartPlotOptions, IHighchartsLegend, IHighchartsNoDataConfiguration, IHighchartsOptions3D, IHighchartsSeriesLabelsSetting, IHighchartsTooltip, ISerieAccessibilitySetting } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import { IHighchartsGaugeYAxis, IHighchartsModelPane } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget"
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
    return deepcopy(descriptor.defaultPieChartPlotOptions) as IHighchartsChartPlotOptions
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

export const getDafaultGaugeChartPlotOptions = () => {
    return deepcopy(descriptor.defaultGaugeChartPlotOptions) as IHighchartsChartPlotOptions
}

export const getDafaultPaneOptions = () => {
    return deepcopy(descriptor.dafaultPaneOptions) as IHighchartsModelPane
}

export const getDefaultGaugsYAxis = () => {
    return deepcopy(descriptor.defaultGaugsYAxis) as IHighchartsGaugeYAxis
}
