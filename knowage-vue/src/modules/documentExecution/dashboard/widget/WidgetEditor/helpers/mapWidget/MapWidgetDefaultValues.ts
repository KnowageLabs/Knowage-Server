import { IMapDialogSettings, IMapTooltipSettings, IMapWidgetBaseLayer, IMapWidgetConditionalStyle, IMapWidgetControlPanel, IMapWidgetLegend, IMapWidgetVisualizationSettings, IMapWidgetVisualizationTypeBalloons, IMapWidgetVisualizationTypeChoropleth, IMapWidgetVisualizationTypeCluster, IMapWidgetVisualizationTypeHeatmap, IMapWidgetVisualizationTypeMarker, IMapWidgetVisualizationTypePie } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import descriptor from './MapWidgetDefaultValuesDescriptor.json'

export const getDefaultMapTooltips = () => {
    return descriptor.defaultTooltips as IMapTooltipSettings
}

export const getDefaultDialogSettings = () => {
    return descriptor.defaultDialogSettings as IMapDialogSettings
}

export const getDefaultLegendSettings = () => {
    return descriptor.defaultLegendSettings as IMapWidgetLegend
}

export const getDefaultBaseLayerSettings = () => {
    return descriptor.defaultBaseLayerSettings as IMapWidgetBaseLayer
}

export const getDefaultControlPanelSettings = () => {
    return descriptor.defaultControlPanelSettings as IMapWidgetControlPanel
}

export const getDefaultConditionalStyle = () => {
    return descriptor.defaultConditionalStyle as IMapWidgetConditionalStyle
}

export const getDefaultVisualizationSettings = () => {
    const visualizationSettings = { types: [] } as IMapWidgetVisualizationSettings
    return visualizationSettings
}

export const getDefaultVisualizationMarkerConfiguration = () => {
    return descriptor.defaultVisualizationMarkerConfiguration as IMapWidgetVisualizationTypeMarker
}

export const getDefaultVisualizationBalloonsConfiguration = () => {
    return descriptor.defaultVisualizationBalloonsConfiguration as IMapWidgetVisualizationTypeBalloons
}

export const getDefaultVisualizationPieConfiguration = () => {
    return descriptor.defaultVisualizationPieConfiguration as IMapWidgetVisualizationTypePie
}

export const getDefaultVisualizationClusterConfiguration = () => {
    return descriptor.defaultVisualizationClusterConfiguration as IMapWidgetVisualizationTypeCluster
}

export const getDefaultVisualizationHeatmapConfiguration = () => {
    return descriptor.defaultVisualizationHeatmapConfiguration as IMapWidgetVisualizationTypeHeatmap
}

export const getDefaultVisualizationChoroplethConfiguration = () => {
    return descriptor.defaultVisualizationChoroplethConfiguration as IMapWidgetVisualizationTypeChoropleth
}