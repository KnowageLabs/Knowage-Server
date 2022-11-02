import { IWidget, IWidgetColumn, IWidgetExports } from "../../Dashboard"
import { ITextWidgetConfiguration, ITextWidgetContent, ITextWidgetSettings } from "../../interfaces/DashboardTextWidget"
import { getFormattedStyle } from "./TextWidgetStyleHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import cryptoRandomString from "crypto-random-string"

const columnNameIdMap = {}

export const formatTextWidget = (widget: any) => {
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ? widget.dataset.dsId[0] : null,
        type: widget.type,
        columns: getFormattedWidgetColumns(widget),
        theme: '',
        settings: {} as ITextWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget) as ITextWidgetSettings
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

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        configuration: getFormattedConfiguration(widget),
        style: getFormattedStyle(widget),
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as ITextWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return {
        content: { text: widget.content?.text ?? '' } as ITextWidgetContent,
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    } as ITextWidgetConfiguration
}