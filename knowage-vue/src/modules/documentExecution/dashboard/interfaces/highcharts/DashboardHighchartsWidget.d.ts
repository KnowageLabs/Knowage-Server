import { IWidgetInteractions, IWidgetResponsive } from "../../Dashboard";
import { HighchartsPieChart } from "../../widget/ChartWidget/classes/highcharts/KnowageHighchartsPieChart";
import { HighchartsPieChartModel } from "./DashboardHighchartsPieChartWidget";

export interface IHighchartsWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: HighchartsPieChart | null,
    configuration: IHighchartsWidgetConfiguration,
    accesssibility: IHighchartsWidgetAccessibility,
    series: IIHighchartsSeriesSetting,
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
        backgroundColor: string
    },
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

export interface HighchartsChartModel {
    lang: {
        noData: string
    },
    chart: {
        options3d: IHighchartsOptions3D
        events: any,
        plotBackgroundColor: any,
        plotBorderWidth: any,
        plotShadow: boolean,
        type: string
    },
    noData: IHighchartsNoDataConfiguration,
    series: IHighchartsChartSerie[],
    settings: any,
    credits: {
        enabled: boolean
    },
    title: {
        text: string
    },
    accessibility: IHighchartsAccessibilitySettings,
    plotOptions: {
        pie: IHighchartsPieChartPlotOptions
    },
    legend: IHighchartsLegend,
    tooltip: IHighchartsTooltip
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
    formatter?: Function,
    formatterText?: string, // TODO - Remove for BE ???
    formatterError?: string  // TODO - Remove for BE
}

export interface IHighchartsChartSerie {
    name: string,
    colorByPoint: boolean,
    groupingFunction: string,
    data: IHighchartsChartSerieData[]
    accessibility: IHighchartsSerieAccessibility,
    label: IHighchartsSerieLabelSettings
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
    backgroundColor: string,
    borderColor: string,
    labelFormat?: string,
    labelFormatter?: Function,
    labelFormatterText?: string, // TODO - Remove for BE ???
    labelFormatterError?: string  // TODO - Remove for BE
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
    formatterText?: string, // TODO - Remove for BE ???
    formatterError?: string  // TODO - Remove for BE
    pointFormatter?: Function,
    pointFormatterText?: string, // TODO - Remove for BE ???
    pointFormatterError?: string  // TODO - Remove for BE
}

export interface IHighchartsSerieLabel {
    enabled: boolean,
    style: {
        fontFamily: string
        fontSize: string
        fontWeight: string
        color: string
        backgroundColor: string
    },
    format: string
}