import { IHighchartsChartDataLabels, IHighchartsSerieAccessibility } from "./DashboardHighchartsWidget"

export interface IHighchartsModelPane {
    startAngle: number,
    endAngle: number,
    center?: [],
    background?: {
        backgroundColor: string,
        innerRadius: string,
        outerRadius: string,
        shape: string
    }
}

export interface IHighchartsGaugeYAxis {
    min: number | null,
    max: number | null,
    tickPosition: string
    tickColor: string
    tickLength: number
    tickWidth: number,
    minorTickInterval: number | 'auto' | null,
    plotBands: IHighchartsBands[]
}
export interface IHighchartsBands {
    from: number,
    to: number,
    color: string,
    thickness: number
}

export interface IHighchartsSeriesDialSettings {
    radius: string,
    backgroundColor: string
    baseWidth: number,
}

export interface IHighchartsSeriesPivotSettings {
    radius: number,
    backgroundColor: string
}

export interface IHighchartsActivityGaugeYAxis {
    lineWidth: 0,
    max: 100,
    min: 0,
    tickPositions: []
}

export interface IHighchartsGaugeSerie {
    name: string,
    data: IHighchartsGaugeSerieData[],
    accessibility?: IHighchartsSerieAccessibility,
    dial?: IHighchartsSeriesDialSettings,
    pivot?: IHighchartsSeriesPivotSettings
}

export interface IHighchartsGaugeSerieData {
    name: string,
    y: number,
    color?: string,
    radius?: string,
    innerRadius?: string,
    dataLabels?: IHighchartsChartDataLabels,
}