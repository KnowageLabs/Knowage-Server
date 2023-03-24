import { IWidgetExports, IWidgetInteractions, IWidgetTitle, IWidgetPaddingStyle, IWidgetBordersStyle, IWidgetShadowsStyle, IWidgetBackgroundStyle, IWidgetResponsive } from './../../Dashboard.d';

export interface IVegaChartsSettings {
    updatable: boolean,
    clickable: boolean,
    chartModel: IVegaChartsModel | null,
    configuration: IVegaChartsConfiguration,
    interactions: IWidgetInteractions,
    chart: IVegaChartSettings,
    style: IVegaChartsStyle,
    tooltip: IVegaChartsTooltipSettings,
    responsive: IWidgetResponsive
}

export interface IVegaChartsModel {
    "$schema": string,
    chart: { type: string },
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
    rotate: number,
    font: string,
    fontSize: {
        field: string
    },
    fontSizeRange: [number, number],
    padding: number
}

export interface IVegaChartsConfiguration {
    textConfiguration: IVegaChartsTextConfiguration,
    noDataConfiguration: IVegaChartsNoDataConfiguration
    exports: IWidgetExports
}

export interface IVegaChartsTextConfiguration {
    font: string,
    minimumFontSize: number,
    maximumFontSize: number,
    wordPadding: number,
    wordAngle: number,
    maxNumberOfWords: number
}

export interface IVegaChartsNoDataConfiguration {
    text: '',
    position: {
        align: string,
        verticalAlign: string
    },
    style: {
        'font-family': string
        'font-size': string
        'font-weight': string
        color: string
        'background-color': string
    }
}

export interface IVegaChartSettings {
    colors: string[]
}

export interface IVegaChartsTooltipSettings {
    prefix: string,
    suffix: string,
    precision: number
}

export interface IVegaChartsStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}