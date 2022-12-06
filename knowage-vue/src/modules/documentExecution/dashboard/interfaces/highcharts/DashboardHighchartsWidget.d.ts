import { IWidgetInteractions, IWidgetResponsive } from "../../Dashboard";
import { HighchartsPieChart } from "../../widget/ChartWidget/classes/highcharts/KnowageHighchartsPieChart";
import { HighchartsPieChartModel } from "./DashboardHighchartsPieChartWidget";

export interface IHighchartsWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: HighchartsPieChart | null,
    configuration: IHighchartsWidgetConfiguration,
    accesssibility: IHighchartsWidgetAccessibility,
    interactions: IWidgetInteractions,
    style: IHighchartsWidgetStyle,
    responsive: IWidgetResponsive
}

export interface IDrillOrderItem {
    orderColumnId: string
    orderColumn: string,
    orderType: "ASC" | "DESC"
}

export interface IHighchartsWidgetConfiguration {
    exports: IWidgetExports
}

export interface IHighchartsWidgetAccessibility {
    seriesAccesibilitySettings: ISerieAccessibilitySetting[]
}

export interface ISerieAccessibilitySetting {
    names: string[]
    accessibility: IHighchartsSerieAccessibility
}

export interface IHighchartsWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}

export interface HighchartsChartModel {
    lang: {
        noData: string
    },
    chart: {
        options3d: HighchartsOptions3D
        events: any,
        plotBackgroundColor: any,
        plotBorderWidth: any,
        plotShadow: boolean,
        type: string
    },
    noData: HighchartsNoDataConfiguration,
    series: IHighchartsChartSerie[],
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
    accessibility: HighchartsAccessibilitySettings,
    plotOptions: {
        pie: IHighchartsPieChartPlotOptions
    },
    legend: {
        enabled: boolean,
        align: string,
        verticalAlign: string,
        layout: string,
    }
}

export interface HighchartsOptions3D {
    enabled: boolean,
    alpha: number,
    beta: number,
    viewDistance: number
}


export interface HighchartsNoDataConfiguration {
    position: {
        align: string,
        verticalAlign: string
    },
    style: {
        fontFamily: string
        fontSize: string
        fontWeight: string
        color: string
        backgroundColor: string
    }
}

export interface HighchartsAccessibilitySettings {
    enabled: boolean,
    description: string,
    keyboardNavigation: {
        enabled: boolean,
        order: string[]
    }
}

export interface IHighchartsPieChartPlotOptions {
    depth: number,
    allowPointSelect: boolean,
    cursor: string,
    dataLabels: IHighchartsPieChartDataLabels
}

export interface IHighchartsPieChartDataLabels {
    enabled: boolean,
    distance: number,
    style: {
        fontFamily: string
        fontSize: string
        fontWeight: string
        color: string
        backgroundColor: string
        textAlign: string
    }
    format?: string,
    formatter?: string,
}

export interface IHighchartsChartSerie {
    name: string,
    colorByPoint: boolean,
    groupingFunction: string,
    data: IHighchartsChartSerieData[]
    accessibility: IHighchartsSerieAccessibility
}

export interface IHighchartsSerieAccessibility {
    enabled: boolean,
    description: string,
    exposeAsGroupOnly: boolean
    keyboardNavigation: { enabled: boolean }
}

export interface IHighchartsChartSerieData {
    name: string,
    y: number,
    sliced?: boolean,
    selected?: boolean
}