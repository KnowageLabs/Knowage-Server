import { IWidgetSelection } from "@/modules/documentExecution/dashboard/Dashboard"
import { IHighchartsAccessibilitySettings, IHighchartsChartPlotOptions, IHighchartsLegend, IHighchartsNoDataConfiguration, IHighchartsOptions3D, IHighchartsSerieAccessibility, IHighchartsSerieLabelSettings, IHighchartsSeriesLabelsSetting, IHighchartsTooltip, ISerieAccessibilitySetting } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import deepcopy from "deepcopy"
import descriptor from './HighchartsDefaultValuesDescriptor.json'

export const getDefaultNoDataConfiguration = () => {
    return deepcopy(descriptor.defaultNoDataConfiguration) as IHighchartsNoDataConfiguration
}

export const getDefaultAccessibilitySettings = () => {
    return deepcopy(descriptor.defaultAccessibilitySettings) as IHighchartsAccessibilitySettings
}

export const getDefaultSerieAccessibilitySetting = () => {
    return deepcopy(descriptor.defaultSerieAccessibilitySetting) as IHighchartsSerieAccessibility
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
    return deepcopy(descriptor.defaultSeriesAccessibilitySettings) as IHighchartsSerieAccessibility
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
    return deepcopy(descriptor.defaultSerieDialSettings)
}

export const getDefaultSeriePivotSettings = () => {
    return deepcopy(descriptor.defaultSeriePivotSettings)
}

export const getDafaultGaugeChartPlotOptions = () => {
    return deepcopy(descriptor.defaultGaugeChartPlotOptions) as IHighchartsChartPlotOptions
}

export const getdefaultActivityGaugeChartPlotOptions = () => {
    return deepcopy(descriptor.defaultActivityGaugeChartPlotOptions) as IHighchartsChartPlotOptions
}

export const getDafaultPaneOptions = () => {
    return deepcopy(descriptor.dafaultPaneOptions)
}

export const getDefaultActivityGaugePaneOptions = () => {
    return deepcopy(descriptor.defaultActivityGaugePaneOptions)
}

export const getDefaultActivityGaugeTooltip = () => {
    const defaultTooltipSettings = deepcopy(descriptor.defaultActivityGaugeTooltip) as any
    defaultTooltipSettings.positioner = function (labelWidth: number) {
        return {
            x: ((this as any).chart.chartWidth - labelWidth) / 2,
            y: (this as any).chart.plotHeight / 2 + 15
        }
    }
    return defaultTooltipSettings
}

export const getDafaultSolidGaugePaneOptions = () => {
    return deepcopy(descriptor.dafaultSolidGaugePaneOptions)
}

export const getDefaultGaugeYAxis = () => {
    return deepcopy(descriptor.defaultGaugeYAxis)
}

export const getDefaultActivityGaugeYAxis = () => {
    return deepcopy(descriptor.defaultActivityGaugeYAxis)
}

export const getDefaultBandsSetting = () => {
    return deepcopy(descriptor.defaultBandsSetting)
}

export const getDefaultHeatmapXAxis = () => {
    return deepcopy(descriptor.defaultHeatmapXAxis)
}

export const getDefaultHeatmapYAxis = () => {
    return deepcopy(descriptor.defaultHeatmapYAxis)
}

export const getDafaultHeatmapPlotOptions = () => {
    return deepcopy(descriptor.dafaultHeatmapPlotOptions) as IHighchartsChartPlotOptions
}

export const getDefaultDateTypeSettings = () => {
    return deepcopy(descriptor.defaultDateTypeSettings)
}

export const getDefaultHeatmapLegendSettings = () => {
    return deepcopy(descriptor.defaultHeatmapLegendSettings)
}