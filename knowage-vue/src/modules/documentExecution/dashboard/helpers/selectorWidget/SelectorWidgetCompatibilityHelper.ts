import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard"
import { ISelectionsWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/DashboardSelectionsWidget"
import cryptoRandomString from 'crypto-random-string'


export const formatSelectorWidget = (widget: any) => {
    console.log('SelectorWidgetCompatibilityHelper - formatSelectorWidget called for: ', widget)
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId,
        type: widget.type,
        columns: getFormattedSelectionColumn(widget),
        conditionalStyles: [],
        theme: '',
        style: {},
        settings: {} as ISelectionsWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget)

    console.log('SelectorWidgetCompatibilityHelper - FORMATTED WIDGET: ', formattedWidget)
    return formattedWidget
}

const getFormattedSelectionColumn = (widget: any) => {
    const formattedColumns = [] as IWidgetColumn[]
    if (widget.content && widget.content.selectedColumn) {
        const formattedColumn = { id: cryptoRandomString({ length: 16, type: 'base64' }), columnName: widget.content.selectedColumn.name, alias: widget.content.selectedColumn.alias, type: widget.content.selectedColumn.type, fieldType: widget.content.selectedColumn.fieldType, multiValue: widget.content.selectedColumn.multiValue, filter: {} } as IWidgetColumn
        formattedColumns.push(formattedColumn)
    }
    return formattedColumns

}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        type: "chips",
        valuesManagement: getFormattedWidgetValuesManagement(widget),
        noSelections: {} as any,
        style: {} as any
    } as ISelectionsWidgetSettings
    return formattedSettings
}

const getFormattedWidgetValuesManagement = (widget: any) => {
    return {} as any
}
