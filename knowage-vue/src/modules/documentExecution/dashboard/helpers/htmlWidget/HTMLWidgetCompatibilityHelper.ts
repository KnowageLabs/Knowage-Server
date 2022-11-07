import { IWidget, IWidgetColumn, IWidgetExports, IWidgetInteractions } from "../../Dashboard"
import { IHTMLWidgetConfiguration, IHTMLWidgetEditor, IHTMLWidgetSettings } from "../../interfaces/DashboardHTMLWidget"
import { getFormattedStyle } from "./HTMLWidgetStyleHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import cryptoRandomString from "crypto-random-string"
import { getFormattedInteractions } from "../common/WidgetInteractionsHelper"

const columnNameIdMap = {}

export const formatHTMLWidget = (widget: any) => {
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ?? null,
        type: widget.type,
        columns: getFormattedWidgetColumns(widget),
        theme: '',
        settings: {} as IHTMLWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget, formattedWidget) as IHTMLWidgetSettings
    return formattedWidget
}

const getFormattedWidgetColumns = (widget: any,) => {
    if (!widget.content || !widget.content.columnSelectedOfDataset) return []
    const formattedColumns = [] as IWidgetColumn[]
    for (let i = 0; i < widget.content.columnSelectedOfDataset.length; i++) {
        formattedColumns.push(getFormattedWidgetColumn(widget.content.columnSelectedOfDataset[i]))
    }
    return formattedColumns
}

const getFormattedWidgetColumn = (widgetColumn: any) => {
    const formattedColumn = { id: cryptoRandomString({ length: 16, type: 'base64' }), columnName: widgetColumn.name, alias: widgetColumn.alias, type: widgetColumn.type, fieldType: widgetColumn.fieldType, multiValue: widgetColumn.multiValue, filter: {} } as IWidgetColumn
    if (widgetColumn.isCalculated) {
        formattedColumn.formula = widgetColumn.formula
        formattedColumn.formulaEditor = widgetColumn.formulaEditor
    }
    columnNameIdMap[formattedColumn.columnName] = formattedColumn.id
    if (widgetColumn.aggregationSelected) formattedColumn.aggregation = widgetColumn.aggregationSelected
    return formattedColumn
}

const getFormattedWidgetSettings = (widget: any, formattedWidget: IWidget) => {
    const formattedSettings = {
        sortingColumn: getColumnId(widget.settings?.sortingColumn) ?? '',
        sortingOrder: widget.settings?.sortingOrder ?? '',
        updatable: widget.updateble,
        clickable: widget.cliccable,
        configuration: getFormattedConfiguration(widget),
        editor: getFormattedEditor(widget, formattedWidget),
        style: getFormattedStyle(widget),
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IHTMLWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return {
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    } as IHTMLWidgetConfiguration
}

const getFormattedEditor = (widget: any, formattedWidget: IWidget) => {
    return {
        css: getFormattedCssToRender(widget.cssToRender, formattedWidget),
        html: widget.htmlToRender,
    } as IHTMLWidgetEditor
}

const getFormattedCssToRender = (cssToRender: string, formattedWidget: IWidget) => {
    if (!cssToRender) return ''
    return cssToRender.replaceAll('[kn-widget-id]', formattedWidget.id as string)
}

const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}