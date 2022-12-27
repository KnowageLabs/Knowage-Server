import { IWidgetSelection } from "@/modules/documentExecution/dashboard/Dashboard"
import { IHighchartsAccessibilitySettings, IHighchartsChartPlotOptions, IHighchartsLegend, IHighchartsNoDataConfiguration, IHighchartsOptions3D, IHighchartsSerieLabelSettings, IHighchartsSeriesLabelsSetting, IHighchartsTooltip, ISerieAccessibilitySetting } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import { IHighchartsBands, IHighchartsGaugeYAxis, IHighchartsModelPane, IHighchartsSeriesDialSettings, IHighchartsSeriesPivotSettings } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget"
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

export const getDefaultSeriesSettings = () => {
    const defaultSeriesSettings = [{
        names: ['all'],
        label: getDefaultSerieLabelSettings()
    }] as IHighchartsSeriesLabelsSetting[]
    return defaultSeriesSettings
}

export const getDefaultSerieLabelSettings = () => {
    return deepcopy(descriptor.defaultSerieLabelSettings) as IHighchartsSerieLabelSettings
}

export const getDefaultSerieDialSettings = () => {
    return deepcopy(descriptor.defaultSerieDialSettings) as IHighchartsSeriesDialSettings
}


export const getDefaultSeriePivotSettings = () => {
    return deepcopy(descriptor.defaultSeriePivotSettings) as IHighchartsSeriesPivotSettings
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

export const getDefaultBandsSetting = () => {
    return deepcopy(descriptor.defaultBandsSetting) as IHighchartsBands
}