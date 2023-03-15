import { IWidgetBackgroundStyle, IWidgetBordersStyle, IWidgetExports, IWidgetInteractions, IWidgetPaddingStyle, IWidgetResponsive, IWidgetShadowsStyle, IWidgetTitle, IWidgetColumn } from '../../Dashboard'

export interface IPivotTableSettings {
    updatable: boolean
    clickable: boolean
    conditionalStyles: IPivotTableWidgetConditionalStyles
    configuration: IPivotTableConfiguration
    visualization: IPivotTableWidgetVisualization
    interactions: IWidgetInteractions
    style: IPivotTableStyle
    responsive: IWidgetResponsive
}

export interface IPivotTableWidgetConditionalStyles {
    enabled: boolean
    conditions: IPivotTableWidgetConditionalStyle[]
}

export interface IPivotTableWidgetVisualization {
    visualizationTypes: IPivotTableWidgetVisualizationTypes
}

export interface IPivotTableWidgetVisualizationTypes {
    enabled: boolean
    types: IPivotTableWidgetVisualizationType[]
}

export interface IPivotTableWidgetVisualizationType {
    target: string | string[]
    prefix: string
    suffix: string,
    precision: number,
    type: string
}

export interface IPivotTableWidgetConditionalStyle {
    target: string
    condition: {
        operator: string
        value: number | null
    }
    properties: {
        'text-align': string
        'font-family': string
        'font-size': string
        'font-style': string
        'font-weight': string
        color: string
        'background-color': string
        icon: string
    }
}

export interface IPivotTableConfiguration {
    rows: IPivotRowsConfiguration
    columns: IPivotColumnsConfiguration
    exports: IWidgetExports
    fieldPicker: IPivotFieldPicker
    fieldPanel: IPivotFieldPanel
}

export interface IPivotTableStyle {
    title: IWidgetTitle
    fields: IPivotTableColumnStyles
    fieldHeaders: IPivotTableColumnStyles
    borders: IWidgetBordersStyle
    background: IWidgetBackgroundStyle
    padding: IWidgetPaddingStyle
    shadows: IWidgetShadowsStyle
    totals: IPivotTotal
    subTotals: IPivotTotal
    columnHeaders: IPivotTableColumnHeadersStyle,
    rowHeaders: IPivotTableColumnHeadersStyle
}

export interface IPivotTableColumnHeadersStyle {
    enabled: boolean,
    properties: {
        "background-color": string,
        color: string,
        "font-family": string,
        "font-size": string,
        "font-style": string,
        "font-weight": string,
        "text-align": string
    }
}

export interface IPivotFields {
    columns: IWidgetColumn[]
    rows: IWidgetColumn[]
    data: IWidgetColumn[]
    filters: IWidgetColumn[]
}

export interface IPivotRowsConfiguration {
    grandTotal: boolean
    subTotal: boolean
    grandTotalLabel: string
    subTotalLabel: string
}

export interface IPivotColumnsConfiguration {
    grandTotal: boolean
    subTotal: boolean
    grandTotalLabel: string
    subTotalLabel: string
}

export interface IPivotFieldPicker {
    enabled: boolean
    width: number
    height: number
}

export interface IPivotFieldPanel {
    enabled: boolean
}
export interface IPivotTooltips {
    target: string | string[]
    enabled: boolean
    prefix: string
    suffix: string
    precision: number
    header: {
        enabled: boolean
        text: string
    }
}
export interface IPivotTotal {
    enabled: boolean
    properties: {
        'font-weight': string
        'font-style': string
        'font-size': string
        'font-family': string
        'text-align': string
        color: string
        'background-color': string
    }
}


export interface IPivotTableColumnStyles {
    enabled: boolean
    styles: IPivotTableColumnStyle[]
}

export interface IPivotTableColumnStyle {
    target: string | string[]
    properties: {
        'background-color': string
        color: string
        'text-align': string
        'font-size': string
        'font-family': string
        'font-style': string
        'font-weight': string
    }
}
