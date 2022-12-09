import { IWidgetInteractions, IWidgetResponsive } from "../../Dashboard";
import { ChartJSPieChartModel } from "./DashboardChartJSPieChartWidget";

export interface IChartJSWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: ChartJSPieChartModel,
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
    data: IChartJSData,
    options: IChartJSOptions
}

export interface IChartJSOptions {
    options: {
        plugin: {
            display: boolean,
            align: string,
            position: string,
        },
        tooltip: {
            enabled: boolean
        },
    },
    tooltip: {
        style: {
            'justify-content': string
            'font-family': string
            'font-size': string
            'font-style': string
            'font-weight': string
            color: string
        },
        backgroundColor: string
    },

}

export interface IChartJSData {
    labels: string[],
    datasets: [
        {
            backgroundColor: string[],
            data: number[]
        }
    ]
}

export interface IChartJSSerie {

}