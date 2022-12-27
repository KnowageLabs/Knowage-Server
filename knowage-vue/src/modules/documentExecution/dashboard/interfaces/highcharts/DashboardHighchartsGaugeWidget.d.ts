export interface IHighchartsModelPane {
    startAngle: number,
    endAngle: number,
    center: []
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
