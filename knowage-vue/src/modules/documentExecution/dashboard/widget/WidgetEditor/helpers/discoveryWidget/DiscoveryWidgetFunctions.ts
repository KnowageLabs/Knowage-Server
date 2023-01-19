import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard"

export const addColumnToDiscoveryWidgetModel = (widgetModel: IWidget, column: IWidgetColumn) => {
    widgetModel.settings.facets.columns.push(column.columnName)
    widgetModel.settings.search.columns.push(column.columnName)
}

export const removeColumnFromDiscoveryWidgetModel = (widgetModel: IWidget, column: IWidgetColumn) => {
    removeColumnNameFromStringArray(widgetModel.settings.facets.columns, column.columnName)
    removeColumnNameFromStringArray(widgetModel.settings.search.columns, column.columnName)
}

const removeColumnNameFromStringArray = (array: string[], columnName: string) => {
    const index = array.findIndex((element: string) => element === columnName)
    if (index !== -1) array.splice(index, 1)
}