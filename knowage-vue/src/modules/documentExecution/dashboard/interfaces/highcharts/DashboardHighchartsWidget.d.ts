import { IWidgetInteractions, IWidgetResponsive } from "../../Dashboard";
import { HighchartsPieChartModel } from "./DashboardHighchartsPieChartWidget";

export interface IHighchartsWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: HighchartsChartModel,
    configuration: IHighchartsWidgetConfiguration,
    interactions: IWidgetInteractions,
    style: IHighchartsWidgetStyle,
    responsive: IWidgetResponsive
}

export interface IDrillOrderItem {
    orderColumnId: string
    orderColumn: string,
    orderType: "asc" | "desc"
}

export interface IHighchartsWidgetConfiguration {
    exports: IWidgetExports
}

export interface IHighchartsWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}

export interface HighchartsChartModel {
    chart: {
        options3d: {
            enabled: boolean,
            alpha: any,
            beta: any,
            viewDistance: any
        }
        events: any,
        plotBackgroundColor: any,
        plotBorderWidth: any,
        plotShadow: boolean,
        type: string
    },
    plotOptions: any,
    series: any[],
    settings: any,
    credits: {
        enabled: boolean
    },
    title: {
        text: string
    },
    tooltip: {
        pointFormat: string
    },
    accessibility: {
        point: {
            valueSuffix: string
        }
    },
    plotOptions: {
        pie: {
            allowPointSelect: boolean,
            cursor: string,
            dataLabels: {
                enabled: boolean,
                format: string
            }
        },
        series: HighchartsChartSerie[]
    },
    legend: {
        enabled: boolean,
        align: string,
        verticalAlign: string,
        layout: string,
    }
}

export interface HighchartsChartSerie {
    name: string,
    colorByPoint: boolean,
    data: HighchartsChartSerieData[]

}

export interface HighchartsChartSerieData {
    name: string,
    y: number,
    sliced?: boolean,
    selected?: boolean
}