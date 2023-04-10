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
    exports: IWidgetExports
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
        'justify-content': string
        'font-family': string
        'font-size': string
        'font-style': string
        'font-weight': string
        color: string
        'background-color': string
        icon: string
    }
}

export interface IMapWidgetLegend {
    // TODO
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