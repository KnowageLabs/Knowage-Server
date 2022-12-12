import { IWidgetInteractions, IWidgetResponsive } from "../../Dashboard";
import { ChartJSPieChartModel } from "./DashboardChartJSPieChartWidget";

export interface IChartJSWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: ChartJSPieChartModel | null,
    configuration: IChartJSWidgetConfiguration,
    interactions: IWidgetInteractions,
    style: IChartJSWidgetStyle,
    responsive: IWidgetResponsive
}

export interface IChartJSWidgetConfiguration {
    exports: IWidgetExports
}

export interface IChartJSWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}

export interface IChartJSChartModel {
    chart: { type: string },
    data: IChartJSData,
    options: IChartJSOptions
}

export interface IChartJSOptions {
    plugins: {
        title: { display: boolean },
        tooltip: IChartJSDefaultTooltipSettings,
        legend: IChartJSDefaultLegendSettings
    }
}

export interface IChartJSDefaultTooltipSettings {
    enabled: boolean,
    titleColor: string,
    titleFont: {
        family: string,
        size: number,
        style: string,
        weight: string
    },
    backgroundColor: string,
    titleAlign: string,
}

export interface IChartJSDefaultLegendSettings {
    display: boolean,
    align: string,
    position: string,
}

export interface IChartJSData {
    labels: string[],
    datasets: IChartJSSerie[]
}

export interface IChartJSSerie {
    backgroundColor: string[],
    data: number[]
}