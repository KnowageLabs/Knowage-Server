import { ITableWidgetColumnStyles, ITableWidgetConditionalStyles, ITableWidgetHeadersStyle, IWidgetBackgroundStyle, IWidgetBordersStyle, IWidgetExports, IWidgetInteractions, IWidgetPaddingStyle, IWidgetResponsive, IWidgetRowsStyle, IWidgetShadowsStyle, IWidgetTitle, IWidgetColumn } from '../../Dashboard'

export interface IPivotTableSettings {
    updatable: boolean
    clickable: boolean
    conditionalStyles: ITableWidgetConditionalStyles
    configuration: IPivotTableConfiguration
    interactions: IWidgetInteractions
    style: IPivotTableStyle
    responsive: IWidgetResponsive
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
    headers: ITableWidgetHeadersStyle
    fields: ITableWidgetColumnStyles
    rows: IWidgetRowsStyle
    background: IWidgetBackgroundStyle
    borders: IWidgetBordersStyle
    padding: IWidgetPaddingStyle
    shadows: IWidgetShadowsStyle
    totals: IPivotTotal
    subTotals: IPivotTotal
    columnHeaders: IPivotTableColumnHeadersStyle
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
        'justify-content': string
        color: string
        'background-color': string
    }
}
