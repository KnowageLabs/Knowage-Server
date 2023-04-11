import { IWidgetInteractions, IWidgetResponsive, IWidgetTitle, IWidgetBordersStyle, IWidgetBackgroundStyle, IWidgetPaddingStyle, IWidgetShadowsStyle, IWidgetExports } from './../../Dashboard.d';

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
    enabled: boolean,
    backgroundLayerId: number | null,
    zoomFactor: number | null,
    showScale: boolean,
    autoCentering: boolean
}

export interface IMapWidgetControlPanel {
    alwaysShow: boolean,
    dimension: string
}

export interface IMapWidgetVisualizationSettings {
    types: IMapWidgetVisualizationType[]
}

export interface IMapWidgetVisualizationType {
    target: string,
    type: string,
    marker: {
        type: string
    }

}

export interface IMapWidgetConditionalStyles {
    enabled: boolean
    conditions: IMapWidgetConditionalStyle[]
}

export interface IMapWidgetConditionalStyle {
    targetLayer: string,
    targetColumn: string,
    condition: {
        type: string
        variable?: string
        parameter?: string
        variableKey?: string
        variablePivotDatasetOptions?: any
        operator: string
        value: string
    },
    properties: {
        'background-color': string
    }
}

export interface IMapWidgetLegend {
    enabled: boolean,
    visualizationType: string,
    position: string,
    alignment: string,
    prefix: string,
    suffix: string,
    precision: number,
    title: IMapWidgetLegendTitle,
    text: IMapWidgetLegendText
}

export interface IMapWidgetLegendTitle {
    text: string,
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
    text: string,
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
    enabled: boolean,
    width: string,
    height: string,
    style: {
        'justify-content': string
        'font-family': string
        'font-size': string
        'font-style': string
        'font-weight': string
        color: string
        'background-color': string
    },
    properties: IMapDialogSettingsProperty[]
}

export interface IMapDialogSettingsProperty {
    layer: string,
    columns: string[]
}

export interface IMapTooltipSettings {
    enabled: boolean,
    layers: { name: string, columns: string[] }[],
}

export interface IMapWidgetStyle {
    title: IWidgetTitle
    borders: IWidgetBordersStyle
    background: IWidgetBackgroundStyle
    padding: IWidgetPaddingStyle
    shadows: IWidgetShadowsStyle
}

export interface IMapWidgetLayer {
    layerId: number,
    name: string,
    descr: string,
    type: string,
    label: string,
    baseLayer: false,
    layerDef: string,
    pathFile: string,
    layerLabel: string,
    layerName: string,
    layerIdentify: string,
    layerURL: any,
    layerOptions: any,
    layerParams: any,
    layerOrder: number,
    category_id: any,
    category: any,
    roles: any,
    properties: string[],
    filebody: any
}