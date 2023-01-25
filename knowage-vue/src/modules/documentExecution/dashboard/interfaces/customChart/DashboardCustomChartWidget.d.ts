import { IWidgetBackgroundStyle, IWidgetBordersStyle, IWidgetExports, IWidgetPaddingStyle, IWidgetShadowsStyle, IWidgetTitle } from "../../Dashboard"

export interface ICustomChartWidgetSettings {
    updatable: boolean
    clickable: boolean
    configuration: ICustomChartWidgetConfiguration
    interactions: IWidgetInteractions
    style: ICustomChartStyle
    responsive: IWidgetResponsive
}

export interface ICustomChartWidgetConfiguration {
    exports: IWidgetExports
}


export interface ICustomChartStyle {
    title: IWidgetTitle
    padding: IWidgetPaddingStyle
    borders: IWidgetBordersStyle
    shadows: IWidgetShadowsStyle
    background: IWidgetBackgroundStyle
}
