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
    tickWidth: number
}