import { ITableWidgetColumnGroup, ITableWidgetColumnGroups, ITableWidgetCustomMessages, ITableWidgetHeaders, ITableWidgetRows, ITableWidgetSummaryRows } from "../../Dashboard"
import * as  tableWidgetDefaultValues from '../../widget/WidgetEditor/helpers/tableWidget/TableWidgetDefaultValues'

export const getFormattedConfiguration = (widget: any) => {
    return { columnGroups: getDefaultColumnGroups(widget), exports: getFormattedExport(widget), headers: getHeadersConfiguration(widget), rows: getFormattedRows(widget), summaryRows: getFormattedSummaryRows(widget), customMessages: getFormattedCustomMessages(widget) as ITableWidgetCustomMessages }
}

const getDefaultColumnGroups = (widget: any) => {
    if (!widget.groups) return tableWidgetDefaultValues.getDefaultColumnGroups()
    const formattedColumnGroups = [] as ITableWidgetColumnGroup[]
    widget.groups.forEach((group: { id: string, name: string }) => formattedColumnGroups.push({ id: group.id, label: group.name, columns: [] }))
    return { enabled: true, groups: formattedColumnGroups } as ITableWidgetColumnGroups

}

export const getFormattedExport = (widget: any) => {
    const formattedExport = tableWidgetDefaultValues.getDefaultExportsConfiguration()
    if (widget.settings.exportpdf) formattedExport.pdf = widget.settings.exportpdf
    if (widget.style) {
        formattedExport.showExcelExport = widget.style.showExcelExport ?? false
        formattedExport.showScreenshot = widget.style.showScreenshot ?? false
    }
    return formattedExport

}

const getHeadersConfiguration = (widget: any) => {
    return { enabled: widget.style?.th?.enabled ?? false, enabledMultiline: widget.style?.th?.multiline ?? false, custom: { enabled: false, rules: [] } } as ITableWidgetHeaders
}

const getFormattedRows = (widget: any) => {
    return { indexColumn: widget.settings?.indexColumn, rowSpan: { enabled: false, column: '' } } as ITableWidgetRows
}

const getFormattedSummaryRows = (widget: any) => {
    let formattedSummaryRows = tableWidgetDefaultValues.getDefaultSummaryRowsConfiguration()
    if (widget.settings.summary) formattedSummaryRows = widget.settings.summary
    if (formattedSummaryRows.list && formattedSummaryRows.list[0]) formattedSummaryRows.list[0].aggregation = 'Columns Default Aggregation'
    return formattedSummaryRows as ITableWidgetSummaryRows
}

export const getFormattedCustomMessages = (widget: any) => {
    if (!widget.settings || !widget.settings.norows) return tableWidgetDefaultValues.getDefaultCustomMessages()

    return { hideNoRowsMessage: widget.settings.norows.hide, noRowsMessage: widget.settings.norows.message } as ITableWidgetCustomMessages
}