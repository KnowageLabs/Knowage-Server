import { IWidgetBackgroundStyle, IWidgetBordersStyle, IWidgetExports, IWidgetInteractions, IWidgetPaddingStyle, IWidgetResponsive, IWidgetShadowsStyle, IWidgetTitle } from "../../Dashboard";
import { ChartJSPieChartModel } from "./DashboardChartJSPieChartWidget";

export interface IChartJSWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: ChartJSPieChartModel | null,
    configuration: IChartJSWidgetConfiguration,
    interactions: IWidgetInteractions,
    chart: IChartJSChartSettings,
    style: IChartJSWidgetStyle,
    responsive: IWidgetResponsive
}

export interface IChartJSWidgetConfiguration {
    exports: IWidgetExports
}

export interface IChartJSChartSettings {
    colors: string[]
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
        tooltip: IChartJSTooltipSettings,
        legend: IChartJSLegendSettings
    },
    events: any[],
    onClick: any,
    responsive?: boolean
    maintainAspectRatio?: boolean
}

export interface IChartJSTooltipSettings {
    enabled: boolean,
    bodyColor: string,
    bodyFont: {
        family: string,
        size: number,
        style: string,
        weight: string
    },
    backgroundColor: string,
    bodyAlign: string,
}

export interface IChartJSLegendSettings {
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

export interface IChartInteractionValues {
    serieName: string,
    serieValue: string,
    categoryName: string,
    categoryValue: string,
    groupingName?: string,
    groupingValue?: string
}
