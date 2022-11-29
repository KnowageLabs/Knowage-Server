import { IWidget, IWidgetExports, IWidgetInteractions } from "../../Dashboard"
import { IHTMLWidgetConfiguration, IHTMLWidgetEditor, IHTMLWidgetSettings } from "../../interfaces/DashboardHTMLWidget"
import { getFormattedStyle } from "./HTMLWidgetStyleHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFormattedInteractions } from "../common/WidgetInteractionsHelper"
import { getFormattedWidgetColumns } from "../common/WidgetColumnHelper"

const columnNameIdMap = {}

export const formatHTMLWidget = (widget: any) => {
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ?? null,
        type: widget.type,
        columns: getFormattedWidgetColumns(widget, columnNameIdMap),
        theme: '',
        settings: {} as IHTMLWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget) as IHTMLWidgetSettings
    return formattedWidget
}


const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        sortingColumn: getColumnId(widget.settings?.sortingColumn) ?? '',
        sortingOrder: widget.settings?.sortingOrder ?? '',
        updatable: widget.updateble,
        clickable: widget.cliccable,
        configuration: getFormattedConfiguration(widget),
        editor: getFormattedEditor(widget),
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

const getFormattedEditor = (widget: any) => {
    return {
        css: widget.cssToRender,
        html: widget.htmlToRender,
    } as IHTMLWidgetEditor
}

const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}