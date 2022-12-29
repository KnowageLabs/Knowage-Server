import { IWidgetInteractions, IWidgetResponsive } from "../../Dashboard";
import { IHighchartsPieChart } from "../../widget/ChartWidget/classes/highcharts/KnowageIHighchartsPieChart";
import { IHighchartsPieChartModel } from "./DashboardIHighchartsPieChartWidget";

export interface IHighchartsWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: IHighchartsPieChart | null,
    configuration: IHighchartsWidgetConfiguration,
    accesssibility: IHighchartsWidgetAccessibility,
    series: IIHighchartsSeriesSetting,
    interactions: IWidgetInteractions,
    chart: IHighchartsChartSettings,
    style: IHighchartsWidgetStyle,
    responsive: IWidgetResponsive
}

export interface IDrillOrderItem {
    orderColumnId: string
    orderColumn: string,
    orderType: "ASC" | "DESC" | ""
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

export interface IIHighchartsSeriesSetting {
    seriesLabelsSettings: IHighchartsSeriesLabelsSetting[]
}

export interface IHighchartsSeriesLabelsSetting {
    names: string[],
    label: IHighchartsSerieLabelSettings
}

export interface IHighchartsSerieLabelSettings {
    enabled: boolean,
    style: {
        fontFamily: string
        fontSize: string
        fontWeight: string
        color: string
    },
    backgroundColor: string
    prefix: string
    suffix: string
    scale: string
    precision: number
    absolute: boolean
    percentage: boolean
}

export interface IHighchartsWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}

export interface IHighchartsChartModel {
    title: string,
    lang: {
        noData: string
    },
    chart: {
        options3d: IHighchartsOptions3D
        type: string
    },
    noData: IHighchartsNoDataConfiguration,
    accessibility: IHighchartsAccessibilitySettings,
    series: IHighchartsChartSerie[],
    settings: IIHighchartsChartModelSettings,
    plotOptions: {
        pie?: IIHighchartsPieChartPlotOptions,
        series?: { events: any }
    },
    legend: IHighchartsLegend,
    tooltip: IHighchartsTooltip,
    credits: {
        enabled: boolean
    }
}

export interface IHighchartsOptions3D {
    enabled: boolean,
    alpha: number,
    beta: number,
    viewDistance: number
}


export interface IHighchartsNoDataConfiguration {
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

export interface IHighchartsAccessibilitySettings {
    enabled: boolean,
    description: string,
    keyboardNavigation: {
        enabled: boolean,
        order: string[]
    }
}

export interface IIHighchartsPieChartPlotOptions {
    showInLegend: boolean,
    depth: number,
    allowPointSelect: boolean,
    cursor: string,
    dataLabels: IIHighchartsPieChartDataLabels,
    colors: string[]
}

export interface IIHighchartsPieChartDataLabels {
    enabled: boolean,
    distance: number,
    style: {
        fontFamily: string
        fontSize: string
        fontWeight: string
        color: string
    },
    position: string
    backgroundColor: string,
    format?: string,
    formatter?: Function,
    formatterText?: string,
    formatterError?: string
}

export interface IHighchartsChartSerie {
    name: string,
    colorByPoint: boolean,
    groupingFunction: string,
    data: IHighchartsChartSerieData[]
    accessibility: IHighchartsSerieAccessibility,
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
    selected?: boolean,
    dataLabels?: IIHighchartsPieChartDataLabels
}

export interface IIHighchartsChartModelSettings {
    drilldown: any, // TODO
    categories: any // TODO
}

export interface IHighchartsLegend {
    enabled: boolean,
    align: string,
    verticalAlign: string,
    layout: string,
    itemStyle: {
        fontFamily: string
        fontSize: string
        fontWeight: string
        color: string
    },
    borderWidth: number,
    backgroundColor: string,
    borderColor: string,
    labelFormat?: string,
    labelFormatter?: Function,
    labelFormatterText?: string,
    labelFormatterError?: string
}

export interface IHighchartsTooltip {
    enabled: boolean,
    style: {
        fontFamily: string
        fontSize: string
        fontWeight: string
        color: string
    },
    backgroundColor: string,
    formatter?: Function,
    formatterText?: string,
    formatterError?: string
    pointFormatter?: Function,
    pointFormatterText?: string,
    pointFormatterError?: string
}

export interface IHighchartsChartSettings {
    colors: string[]
}

export interface IHighchartsDrilldown {
    enabled: boolean
}
