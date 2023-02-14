import { IWidgetBackgroundStyle, IWidgetBordersStyle, IWidgetExports, IWidgetPaddingStyle, IWidgetShadowsStyle, IWidgetTitle } from "../../Dashboard"

export interface ICustomChartWidgetSettings {
    updatable: boolean
    clickable: boolean
    editor: ICustomChartWidgetEditor
    configuration: ICustomChartWidgetConfiguration
    interactions: IWidgetInteractions
    style: ICustomChartStyle
    responsive: IWidgetResponsive
}


export interface ICustomChartWidgetEditor {
    css: string,
    html: string,
    js: string
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
