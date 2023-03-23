import { IWidgetExports, IWidgetInteractions, IWidgetTitle, IWidgetPaddingStyle, IWidgetBordersStyle, IWidgetShadowsStyle, IWidgetBackgroundStyle, IWidgetResponsive } from './../../Dashboard.d';

export interface IVegaChartsSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: IVegaChartsModel | null,
    configuration: IVegaChartsConfiguration,
    interactions: IWidgetInteractions,
    chart: IVegaChartSettings,
    style: IVegaChartsStyle,
    responsive: IWidgetResponsive
}

export interface IVegaChartsModel {
    "$schema": string,
    description: string,
    padding: number,
    autosize: {
        type: string,
        contains: string
    },
    signals: IVegaChartsSignal[],
    data: IVegaChartsData[],
    scales: IVegaChartsScale[],
    marks: IVegaChartsMark[]
}

export interface IVegaChartsData {
    name: string,
    values: { text: string, count: number }[],
    transform: { type: string, as: string, expr: string }[]
}

export interface IVegaChartsSignal {
    name: string,
    init: string,
    on: { events: string, update: string }[]
}

export interface IVegaChartsScale {
    name: string,
    type: string,
    domain: {
        data: string,
        field: string
    },
    range: string[]
}

export interface IVegaChartsMark {
    type: string,
    from: {
        data: string
    },
    encode: IVegaChartsMarkEncode,
    transform: IVegaChartsMarkTransform[]
}

export interface IVegaChartsMarkEncode {
    enter: {
        text: {
            field: string
        },
        align: {
            value: string
        },
        baseline: {
            value: string
        },
        fill: {
            scale: string,
            field: string
        },
        tooltip: {
            signal: string
        }
    },
    update: {
        fillOpacity: {
            value: number
        }
    },
    hover: {
        fillOpacity: {
            value: number
        }
    }
}

export interface IVegaChartsMarkTransform {
    type: string,
    text: {
        field: string
    },
    rotate: {
        field: string
    },
    font: string,
    fontSize: {
        field: string
    },
    fontSizeRange: [number, number],
    padding: number
}

export interface IVegaChartsConfiguration {
    exports: IWidgetExports
}


export interface IVegaChartSettings {
    colors: string[]
}

export interface IVegaChartsStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}