import { IWidgetInteractions, IWidgetResponsive } from "../../Dashboard";
import { IHighchartsGaugeSerie, IHighchartsSeriesDialSettings, IHighchartsSeriesPivotSettings, IHighchartsSolidGaugePlotOptions } from "./DashboardHighchartsGaugeWidget";

export interface IHighchartsWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: IHighchartsChartModel | null,
    configuration: IHighchartsWidgetConfiguration,
    accesssibility: IHighchartsWidgetAccessibility,
    series: IHighchartsSeriesSetting,
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

export interface IHighchartsSeriesSetting {
    seriesLabelsSettings: IHighchartsSeriesLabelsSetting[]
}

export interface IHighchartsSeriesLabelsSetting {
    names: string[],
    label: IHighchartsSerieLabelSettings,
    dial?: IHighchartsSeriesDialSettings,
    pivot?: IHighchartsSeriesPivotSettings
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
    series: (IHighchartsChartSerie | IHighchartsGaugeSerie)[],
    settings: IIHighchartsChartModelSettings,
    plotOptions: {
        pie?: IHighchartsChartPlotOptions,
        gauge?: IHighchartsChartPlotOptions,
        solidgauge?: IHighchartsChartPlotOptions
        series?: { events: any }
    },
    legend: IHighchartsLegend,
    tooltip: IHighchartsTooltip,
    colors: string[]
    credits: {
        enabled: boolean
    },
    pane?: IHighchartsModelPane,
    yAxis?: IHighchartsGaugeYAxis
}

export interface IHighchartsChartPlotOptions {
    showInLegend: boolean,
    depth: number,
    allowPointSelect: boolean,
    cursor: string,
    dataLabels: IHighchartsChartDataLabels,
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

export interface IHighchartsChartDataLabels {
    enabled: boolean,
    distance?: number,
    style: {
        fontFamily: string
        fontSize: string
        fontWeight: string
        color: string
    },
    position: string
    backgroundColor: string,
    linecap?: string,
    stickyTracking?: boolean,
    rounded?: boolean,
    format?: string,
    formatter?: Function,
    formatterText?: string,
    formatterError?: string,
    y?: number
}

export interface IHighchartsChartSerie {
    name: string,
    data: IHighchartsChartSerieData[]
    accessibility?: IHighchartsSerieAccessibility,
    colorByPoint?: boolean,
    groupingFunction?: string,
}

export interface IHighchartsChartSerieData {
    name: string,
    y: number,
    sliced?: boolean,
    selected?: boolean,
    dataLabels?: IHighchartsChartDataLabels,
}

export interface IHighchartsSerieAccessibility {
    enabled: boolean,
    description: string,
    exposeAsGroupOnly: boolean
    keyboardNavigation: { enabled: boolean }
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
