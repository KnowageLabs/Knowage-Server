import { IMapDialogSettings, IMapTooltipSettings, IMapWidgetBaseLayer, IMapWidgetConditionalStyle, IMapWidgetControlPanel, IMapWidgetLegend, IMapWidgetVisualizationSettings, IMapWidgetVisualizationTypeBalloons, IMapWidgetVisualizationTypeChoropleth, IMapWidgetVisualizationTypeCluster, IMapWidgetVisualizationTypeHeatmap, IMapWidgetVisualizationTypeMarker, IMapWidgetVisualizationTypePie } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import descriptor from './MapWidgetDefaultValuesDescriptor.json'
import deepcopy from 'deepcopy'

export const getDefaultMapTooltips = () => {
    return deepcopy(descriptor.defaultTooltips) as IMapTooltipSettings
}

export const getDefaultDialogSettings = () => {
    return deepcopy(descriptor.defaultDialogSettings) as IMapDialogSettings
}

export const getDefaultLegendSettings = () => {
    return deepcopy(descriptor.defaultLegendSettings) as IMapWidgetLegend
}

export const getDefaultBaseLayerSettings = () => {
    return deepcopy(descriptor.defaultBaseLayerSettings) as IMapWidgetBaseLayer
}

export const getDefaultControlPanelSettings = () => {
    return deepcopy(descriptor.defaultControlPanelSettings) as IMapWidgetControlPanel
}

export const getDefaultConditionalStyle = () => {
    return deepcopy(descriptor.defaultConditionalStyle) as IMapWidgetConditionalStyle
}

export const getDefaultVisualizationSettings = () => {
    const visualizationSettings = {
        types: [{
            target: [],
            type: 'markers',
            markerConf: getDefaultVisualizationMarkerConfiguration(),
            balloonConf: getDefaultVisualizationBalloonsConfiguration(),
            pieConf: getDefaultVisualizationPieConfiguration(),
            clusterConf: getDefaultVisualizationClusterConfiguration(),
            heatmapConf: getDefaultVisualizationHeatmapConfiguration(),
            analysisConf: getDefaultVisualizationChoroplethConfiguration()
        }]
    } as IMapWidgetVisualizationSettings
    return deepcopy(visualizationSettings)
}

export const getDefaultVisualizationMarkerConfiguration = () => {
    return deepcopy(descriptor.defaultVisualizationMarkerConfiguration) as IMapWidgetVisualizationTypeMarker
}

export const getDefaultVisualizationBalloonsConfiguration = () => {
    return deepcopy(descriptor.defaultVisualizationBalloonsConfiguration) as IMapWidgetVisualizationTypeBalloons
}

export const getDefaultVisualizationPieConfiguration = () => {
    return deepcopy(descriptor.defaultVisualizationPieConfiguration) as IMapWidgetVisualizationTypePie
}

export const getDefaultVisualizationClusterConfiguration = () => {
    return deepcopy(descriptor.defaultVisualizationClusterConfiguration) as IMapWidgetVisualizationTypeCluster
}

export const getDefaultVisualizationHeatmapConfiguration = () => {
    return deepcopy(descriptor.defaultVisualizationHeatmapConfiguration) as IMapWidgetVisualizationTypeHeatmap
}

export const getDefaultVisualizationChoroplethConfiguration = () => {
    return deepcopy(descriptor.defaultVisualizationChoroplethConfiguration) as IMapWidgetVisualizationTypeChoropleth
}