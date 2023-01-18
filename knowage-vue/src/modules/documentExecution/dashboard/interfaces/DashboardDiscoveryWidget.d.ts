import { IWidgetInteractions, IWidgetResponsive } from "../Dashboard";

export interface IDiscoveryWidgetSettings {
    updatable: boolean
    clickable: boolean
    configuration: IDiscoveryWidgetConfiguration
    facets: IDiscoveryWidgetFacetsSettings
    search: IDiscoveryWidgetSearchSettings,
    interactions: IWidgetInteractions
    style: ITableWidgetStyle
    tooltips: ITableWidgetTooltipStyle[]
    responsive: IWidgetResponsive
}

export interface IDiscoveryWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}

export interface IDiscoveryWidgetFacetsSettings {

}

export interface IDiscoveryWidgetSearchSettings {

}

export interface IDiscoveryWidgetConfiguration {
    exports: IWidgetExports
    customMessages: ITableWidgetCustomMessages
}