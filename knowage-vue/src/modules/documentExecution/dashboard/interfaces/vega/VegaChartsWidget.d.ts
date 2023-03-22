import { IWidgetExports, IWidgetInteractions, IWidgetTitle, IWidgetPaddingStyle, IWidgetBordersStyle, IWidgetShadowsStyle, IWidgetBackgroundStyle, IWidgetResponsive } from './../../Dashboard.d';

export interface IVegaChartsSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: IVegaChartsModel | null,
    configuration: IVegaChartsConfiguration,
    interactions: IWidgetInteractions,
    chart: IVegaChartSettings,
    style: IVegaChartsStyle,
    responsive: IWidgetResponsive
}

export interface IVegaChartsModel {

}

export interface IVegaChartsConfiguration {
    exports: IWidgetExports
}


export interface IVegaChartSettings {
    colors: string[]
}

export interface IVegaChartsStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}