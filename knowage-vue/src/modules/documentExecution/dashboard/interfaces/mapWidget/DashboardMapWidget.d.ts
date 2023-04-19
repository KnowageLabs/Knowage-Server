import { IWidgetInteractions, IWidgetResponsive, IWidgetTitle, IWidgetBordersStyle, IWidgetBackgroundStyle, IWidgetPaddingStyle, IWidgetShadowsStyle, IWidgetExports, IDataset, IIcon } from './../../Dashboard.d'

export interface IMapWidgetSettings {
    updatable: boolean
    clickable: boolean
    configuration: IMapWidgetConfiguration
    visualization: IMapWidgetVisualizationSettings
    conditionalStyles: IMapWidgetConditionalStyles
    legend: IMapWidgetLegend
    dialog: IMapDialogSettings
    tooltips: IMapTooltipSettings
    interactions: IWidgetInteractions
    style: IMapWidgetStyle
    responsive: IWidgetResponsive
}

export interface IMapWidgetConfiguration {
    baseLayer: IMapWidgetBaseLayer
    controlPanel: IMapWidgetControlPanel
    exports: IWidgetExports
}

export interface IMapWidgetBaseLayer {
    enabled: boolean
    backgroundLayerId: number | null
    zoomFactor: number | null
    showScale: boolean
    autoCentering: boolean
}

export interface IMapWidgetControlPanel {
    alwaysShow: boolean
    dimension: string
}

export interface IMapWidgetVisualizationSettings {
    types: IMapWidgetVisualizationType[]
}

export interface IMapWidgetVisualizationType {
    target: string[]
    type: string
    markerConf?: IMapWidgetVisualizationTypeMarker
    balloonConf?: IMapWidgetVisualizationTypeBalloons
    pieConf?: IMapWidgetVisualizationTypePie
    clusterConf?: IMapWidgetVisualizationTypeCluster
    heatmapConf?: IMapWidgetVisualizationTypeHeatmap
    analysisConf?: IMapWidgetVisualizationTypeChoropleth
}

export interface IMapWidgetVisualizationTypeMarker {
    type: string
    style: {
        color?: string
        borderColor?: string
    }
    size?: number
    icon?: IIcon
    scale?: number
    url?: string
    img?: string
}

export interface IMapWidgetVisualizationTypeBalloons {
    borderColor: string
    fromColor: string
    toColor: string
    minSize: number
    maxSize: number
    method: string
    classes: number
    properties?: {
        thresholds: { color: string; from: number; to: number }[]
    }
}

export interface IMapWidgetVisualizationTypePie {
    categorizeBy: string
    type: string
    borderColor: string
    fromColor: string
    toColor: string
    minSize: number
    maxSize: number
}

export interface IMapWidgetVisualizationTypeCluster {
    enabled: boolean
    radiusSize: number
    style: {
        'font-size'?: string
        color?: string
        'background-color'?: string
    }
}

export interface IMapWidgetVisualizationTypeHeatmap {
    radius: number
    blur: number
}

export interface IMapWidgetVisualizationTypeChoropleth {
    method: string
    classes: number
    fromColor: string
    toColor: string
    borderColor: string
    properties?: {
        thresholds: { color: string; from: number; to: number }[]
    }
}

export interface IMapWidgetConditionalStyles {
    enabled: boolean
    conditions: IMapWidgetConditionalStyle[]
}

export interface IMapWidgetConditionalStyle {
    targetLayer: string
    targetColumn: string
    condition: {
        type: string
        variable?: string
        parameter?: string
        variableKey?: string
        variablePivotDatasetOptions?: any
        operator: string
        value: string
    }
    properties: {
        'background-color': string
    }
}

export interface IMapWidgetLegend {
    enabled: boolean
    visualizationType: string
    position: string
    alignment: string
    prefix: string
    suffix: string
    precision: number
    title: IMapWidgetLegendTitle
    text: IMapWidgetLegendText
}

export interface IMapWidgetLegendTitle {
    text: string
    style: {
        'justify-content': string
        'font-family': string
        'font-size': string
        'font-style': string
        'font-weight': string
        color: string
        'background-color': string
    }
}

export interface IMapWidgetLegendText {
    text: string
    style: {
        'justify-content': string
        'font-family': string
        'font-size': string
        'font-style': string
        'font-weight': string
        color: string
        'background-color': string
    }
}

export interface IMapDialogSettings {
    enabled: boolean
    width: string
    height: string
    style: {
        'justify-content': string
        'font-family': string
        'font-size': string
        'font-style': string
        'font-weight': string
        color: string
        'background-color': string
    }
    properties: IMapDialogSettingsProperty[]
}

export interface IMapDialogSettingsProperty {
    layer: string
    columns: string[]
}

export interface IMapTooltipSettings {
    enabled: boolean
    layers: { name: string; columns: string[] }[]
}

export interface IMapWidgetStyle {
    title: IWidgetTitle
    borders: IWidgetBordersStyle
    background: IWidgetBackgroundStyle
    padding: IWidgetPaddingStyle
    shadows: IWidgetShadowsStyle
}

export interface ILayer {
    layerId: number
    name: string
    descr: string
    type: string
    label: string
    baseLayer: false
    layerDef: string
    pathFile: string
    layerLabel: string
    layerName: string
    layerIdentify: string
    layerURL: any
    layerOptions: any
    layerParams: any
    layerOrder: number
    category_id: any
    category: any
    roles: any
    properties: string[]
    filebody: any
}

export interface IMapWidgetLayer {
    type: string
    dsId: number
    alias: string
    name: string
    defaultVisible: boolean
    dataset: IDataset | any
    content: {
        columnSelectedOfDataset: IWidgetMapLayerColumn[]
    }
    order: number
    targetDefault: boolean
    hasShownDetails: boolean
    defaultIndicator: string
    layerID: string
    isStatic: boolean
    showTooltip: boolean
    tooltipColumn: string
    visualizationType: string
    markerConf: any
    clusterConf: any
    heatmapConf: any
    analysisConf: any
    modalSelectionColumn: string
    datasetLink?: number
    datasetColumnLink?: number
    catalogLayerLink?: number
    catalogLayerColumnLink?: number
}

export interface IWidgetMapLayerColumn {
    name: string
    alias: string
    type: string
    properties: {
        aggregateBy: boolean
        coordType: string
        coordFormat: string
        showTooltip: boolean
        modal: boolean
        showMap?: boolean
        showFilter?: boolean
    }
    fieldType: string
    multiValue: boolean
    precision: number
    scale: number
    personal: boolean
    decrypt: boolean
    subjectId: boolean
    aliasToShow: string
    aggregationSelected?: string
    deleted?: boolean
}
