import { IHighchartsHeatmapAxis } from './../../../../../interfaces/highcharts/DashboardHighchartsHeatmapWidget.d';
import { IWidgetSelection } from "@/modules/documentExecution/dashboard/Dashboard"
import { IHighchartsAccessibilitySettings, IHighchartsChartPlotOptions, IHighchartsLegend, IHighchartsNoDataConfiguration, IHighchartsOptions3D, IHighchartsSerieAccessibility, IHighchartsSerieLabelSettings, IHighchartsSeriesLabelsSetting, IHighchartsTooltip, ISerieAccessibilitySetting } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import { IHighchartsBands, IHighchartsGaugeYAxis, IHighchartsModelPane, IHighchartsSeriesDialSettings, IHighchartsSeriesPivotSettings, IHighchartsGaugeActivityTooltip } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsGaugeWidget"
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
    return deepcopy(descriptor.defaultSerieDialSettings) as IHighchartsSeriesDialSettings
}

export const getDefaultSeriePivotSettings = () => {
    return deepcopy(descriptor.defaultSeriePivotSettings) as IHighchartsSeriesPivotSettings
}

export const getDafaultGaugeChartPlotOptions = () => {
    return deepcopy(descriptor.defaultGaugeChartPlotOptions) as IHighchartsChartPlotOptions
}

export const getdefaultActivityGaugeChartPlotOptions = () => {
    return deepcopy(descriptor.defaultActivityGaugeChartPlotOptions) as IHighchartsChartPlotOptions
}

export const getDafaultPaneOptions = () => {
    return deepcopy(descriptor.dafaultPaneOptions) as IHighchartsModelPane
}

export const getDefaultActivityGaugePaneOptions = () => {
    return deepcopy(descriptor.defaultActivityGaugePaneOptions) as IHighchartsModelPane
}

export const getDefaultActivityGaugeTooltip = () => {
    const defaultTooltipSettings = deepcopy(descriptor.defaultActivityGaugeTooltip) as IHighchartsGaugeActivityTooltip
    defaultTooltipSettings.positioner = function (labelWidth) {
        return {
            x: ((this as any).chart.chartWidth - labelWidth) / 2,
            y: (this as any).chart.plotHeight / 2 + 15
        }
    }
    return defaultTooltipSettings
}


export const getDafaultSolidGaugePaneOptions = () => {
    return deepcopy(descriptor.dafaultSolidGaugePaneOptions) as IHighchartsModelPane
}

export const getDefaultGaugeYAxis = () => {
    return deepcopy(descriptor.defaultGaugeYAxis) as IHighchartsGaugeYAxis
}

export const getDefaultActivityGaugeYAxis = () => {
    return deepcopy(descriptor.defaultActivityGaugeYAxis) as IHighchartsGaugeYAxis
}

export const getDefaultBandsSetting = () => {
    return deepcopy(descriptor.defaultBandsSetting) as IHighchartsBands
}

export const getDefaultHeatmapXAxis = () => {
    return deepcopy(descriptor.defaultHeatmapXAxis) as IHighchartsHeatmapAxis
}

export const getDefaultHeatmapYAxis = () => {
    return deepcopy(descriptor.defaultHeatmapYAxis) as IHighchartsHeatmapAxis
}


