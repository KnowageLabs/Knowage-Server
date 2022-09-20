import { ITableWidgetColumnGroup, ITableWidgetColumnGroups, ITableWidgetCustomMessages, ITableWidgetRows, ITableWidgetSummaryRows, IWidget } from "../../Dashboard"

export const getFormattedConfiguration = (widget: any) => {
    return { columnGroups: getFormattedColumnGroups(widget), exports: getFormattedExport(widget), headers: getHeadersConfiguration(widget), rows: getFormattedRows(widget), summaryRows: getFormattedSummaryRows(widget), customMessages: getFormattedCustomMessages(widget) as ITableWidgetCustomMessages }
}

const getFormattedColumnGroups = (widget: any) => {
    if (!widget.groups) return []
    const formattedColumnGroups = [] as ITableWidgetColumnGroup[]
    widget.groups.forEach((group: { id: string, name: string }) => formattedColumnGroups.push({ id: group.id, label: group.name, columns: [] }))
    return { enabled: true, groups: formattedColumnGroups } as ITableWidgetColumnGroups

}

const getFormattedExport = (widget: any) => {
    const formattedExport = {
        pdf: {
            enabled: false,
            custom: {
                height: 0,
                width: 0,
                enabled: false
            },
            a4landscape: false,
            a4portrait: false
        },
        showExcelExport: false,
        showScreenshot: false
    }
    if (widget.settings.exportpdf) formattedExport.pdf = widget.settings.exportpdf
    if (widget.style) {
        formattedExport.showExcelExport = widget.style.showExcelExport ?? false
        formattedExport.showScreenshot = widget.style.showScreenshot ?? false
    }
    return formattedExport

}

const getHeadersConfiguration = (widget: any) => {
    return { enabled: widget.style?.th?.enabled ?? false, enabledMultiline: widget.style?.th?.multiline ?? false, custom: { enabled: false, rules: [] } }
}

const getFormattedRows = (widget: any) => {
    return { indexColumn: widget.settings?.indexColumn, rowSpan: { enabled: false, column: '' } } as ITableWidgetRows
}

const getFormattedSummaryRows = (widget: any) => {
    let formattedSummaryRows = {} as ITableWidgetSummaryRows
    if (widget.settings.summary) formattedSummaryRows = widget.settings.summary
    if (formattedSummaryRows.list && formattedSummaryRows.list[0]) formattedSummaryRows.list[0].aggregation = 'Columns Default Aggregation'
    return formattedSummaryRows
}

const getFormattedCustomMessages = (widget: any) => {
    if (!widget.settings || !widget.settings.norows) return {
        hideNoRowsMessage: false,
        noRowsMessage: ''
    } as ITableWidgetCustomMessages

    return { hideNoRowsMessage: widget.settings.norows.hide, noRowsMessage: widget.settings.norows.message } as ITableWidgetCustomMessages
}