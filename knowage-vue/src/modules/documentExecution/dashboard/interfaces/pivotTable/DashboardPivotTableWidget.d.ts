import { ITableWidgetStyle, IWidgetInteractions } from "../../Dashboard"

export interface IPivotTableSettings {
    updatable: boolean
    clickable: boolean
    conditionalStyles: ITableWidgetConditionalStyles  // TODO - Darko see if needed or it needs to be changed to use another interface (probably)
    configuration: IPivotTableConfiguration
    interactions: IWidgetInteractions
    style: IPivotTableStyle
    responsive: IWidgetResponsive
}

export interface IPivotTableConfiguration {
    exports: IWidgetExports
}

export interface IPivotTableStyle {
    title: IWidgetTitle
    borders: IWidgetBordersStyle
    columns: ITableWidgetColumnStyles
    headers: ITawbleWidgetHeadersStyle
    padding: IWidgetPaddingStyle
    rows: IWidgetRowsStyle
    shadows: IWidgetShadowsStyle
    summary: ITableWidgetSummaryStyle
    background: IWidgetBackgroundStyle
}