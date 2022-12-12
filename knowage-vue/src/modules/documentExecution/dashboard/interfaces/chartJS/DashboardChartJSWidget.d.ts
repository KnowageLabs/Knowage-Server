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
    data: IChartJSData,
    options: IChartJSOptions
}

export interface IChartJSOptions {
    options: {
        plugins: {
            tooltip: {
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

            },
            legend: {
                display: boolean,
                align: string,
                position: string,
            }
        }

    }

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