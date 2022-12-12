import { IWidgetColumn } from "../../Dashboard"

interface IOldModelCategory {
    column: string
    groupby: string
    groupbyNames: string,
    name: string,
    orderColumn: string,
    orderType: string,
    drillOrder: any
}

const columnNameIdMap = {}

export const addCategoryColumns = (category: IOldModelCategory, formattedColumns: IWidgetColumn[], widgetColumNameMap: any) => {
    addCategoryColumn(category, widgetColumNameMap, formattedColumns)
    if (category.groupbyNames) {
        addDrillColumnsFromCategory(category, widgetColumNameMap, formattedColumns)
    }
}

const addCategoryColumn = (category: IOldModelCategory, widgetColumNameMap: any, formattedColumns: IWidgetColumn[]) => {
    if (widgetColumNameMap[category.column]) {
        const tempColumn = { ...widgetColumNameMap[category.column] }
        if (category.drillOrder) tempColumn.drillOrder = createDrillOrder(category.drillOrder[category.column].orderColumn, category.drillOrder[category.column].orderType)
        formattedColumns.push(tempColumn)

    }
}

const addDrillColumnsFromCategory = (category: IOldModelCategory, widgetColumNameMap: any, formattedColumns: IWidgetColumn[]) => {
    const categoryColumnNames = category.groupbyNames.split(',')
    categoryColumnNames.forEach((columnName: string) => {
        const columnNameTrimmed = columnName.trim()
        if (widgetColumNameMap[columnNameTrimmed]) {
            const tempColumn = { ...widgetColumNameMap[columnNameTrimmed], drillOrder: createDrillOrder(null, '') }
            if (category.drillOrder && category.drillOrder[columnNameTrimmed]) {
                tempColumn.drillOrder = createDrillOrder(category.drillOrder[columnNameTrimmed].orderColumn, category.drillOrder[columnNameTrimmed].orderType)
            }
            formattedColumns.push(tempColumn)
        }
    })
}

const createDrillOrder = (orderColumn: string | null, orderType: string) => {
    return orderColumn ? { orderColumnId: orderColumn ? getColumnId(orderColumn) : '', orderColumn: orderColumn, orderType: orderType ? orderType.toUpperCase() : '' } : { orderColumnId: '', orderColumn: '', orderType: '' }
}


export const addSerieColumn = (serie: any, widgetColumNameMap: any, formattedColumns: IWidgetColumn[]) => {
    const tempColumn = widgetColumNameMap[serie.column] as IWidgetColumn
    tempColumn.aggregation = serie.groupingFunction
    if (serie.orderType) tempColumn.orderType = serie.orderType.toUpperCase()
    formattedColumns.push(tempColumn)
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}


