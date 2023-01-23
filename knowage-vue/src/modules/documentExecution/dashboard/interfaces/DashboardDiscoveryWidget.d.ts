import { ITableWidgetColumnStyles, ITableWidgetCustomMessages, ITableWidgetPagination, IWidgetBackgroundStyle, IWidgetBordersStyle, IWidgetExports, IWidgetInteractions, IWidgetPaddingStyle, IWidgetResponsive, IWidgetShadowsStyle, IWidgetTitle } from '../Dashboard'

export interface IDiscoveryWidgetSettings {
    updatable: boolean
    clickable: boolean
    configuration: IDiscoveryWidgetConfiguration
    facets: IDiscoveryWidgetFacetsSettings
    search: IDiscoveryWidgetSearchSettings
    interactions: IWidgetInteractions
    style: IDiscoveryWidgetStyle
    tooltips: ITableWidgetTooltipStyle[]
    responsive: IWidgetResponsive
}

export interface IDiscoveryWidgetStyle {
    title: IWidgetTitle
    borders: IWidgetBordersStyle
    columns: ITableWidgetColumnStyles
    padding: IWidgetPaddingStyle
    rows: IWidgetRowsStyle
    shadows: IWidgetShadowsStyle
    background: IWidgetBackgroundStyle
}

export interface IDiscoveryWidgetFacetsSettings {
    columns: string[]
    enabled: boolean
    selection: boolean
    closedByDefault: boolean
    width: string
    limit: number | null
    precision: number | null
}

export interface IDiscoveryWidgetSearchSettings {
    columns: string[]
    enabled: boolean
    default: boolean
    defaultType: 'static' | 'driver'
    defaultValue: string
    driverLabel?: string
    facetSearchParams: any
}

export interface IDiscoveryWidgetConfiguration {
    exports: IWidgetExports
    customMessages: ITableWidgetCustomMessages
}
