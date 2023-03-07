import {
    IPivotColumnsConfiguration,
    IPivotFieldPicker,
    IPivotRowsConfiguration,
    ITableWidgetColumnStyles,
    ITableWidgetConditionalStyles,
    ITableWidgetHeadersStyle,
    IWidgetBackgroundStyle,
    IWidgetBordersStyle,
    IWidgetExports,
    IWidgetInteractions,
    IWidgetPaddingStyle,
    IWidgetResponsive,
    IWidgetRowsStyle,
    IWidgetShadowsStyle,
    IWidgetTitle,
    IPivotFieldPanel,
    IPivotTotal
} from '../../Dashboard'

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
}
