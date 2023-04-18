import { IWidgetExports, IWidgetTitle, IWidgetPaddingStyle, IWidgetBordersStyle, IWidgetShadowsStyle, IWidgetBackgroundStyle } from './../../Dashboard.d';
import { IWidgetInteractions, IWidgetResponsive } from "../../Dashboard";


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
    datetypeSettings?: any
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
    dial?: any,
    pivot?: any,
    serieColor?: string,
    serieColorEnabled?: boolean
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
        type: string,
        backgroundColor?: any
    },
    noData: IHighchartsNoDataConfiguration,
    accessibility: IHighchartsAccessibilitySettings,
    series: any[],
    settings: IHighchartsChartModelSettings,
    plotOptions: {
        pie?: IHighchartsChartPlotOptions,
        gauge?: IHighchartsChartPlotOptions,
        solidgauge?: IHighchartsChartPlotOptions
        heatmap?: IHighchartsChartPlotOptions
        series?: { events: any }
    },
    legend: any,
    tooltip: any,
    colors: string[]
    credits: {
        enabled: boolean
    },
    pane?: any,
    xAxis?: any
    yAxis?: any,
    colorAxis?: { stops: any[] }
}

export interface IHighchartsChartPlotOptions {
    showInLegend: boolean,
    depth: number,
    allowPointSelect: boolean,
    cursor: string,
    connectNulls?: boolean,
    nullColor?: string,
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
    backgroundColor: string | null,
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
    dataLabels?: IHighchartsChartDataLabels
}

export interface IHighchartsSerieAccessibility {
    enabled: boolean,
    description: string,
    exposeAsGroupOnly: boolean
    keyboardNavigation: { enabled: boolean }
}

export interface IHighchartsChartModelSettings {
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
    valuePrefix?: string,
    valueSuffix?: string,
    valueDecimals?: number,
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
