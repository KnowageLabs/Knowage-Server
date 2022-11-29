import { IWidgetColumn } from "../../Dashboard"
import cryptoRandomString from "crypto-random-string"

export const getFormattedWidgetColumns = (widget: any, columnNameIdMap: any, allowDuplicates: boolean = true) => {
    if (!widget.content || !widget.content.columnSelectedOfDataset) return []
    const formattedColumns = [] as IWidgetColumn[]
    for (let i = 0; i < widget.content.columnSelectedOfDataset.length; i++) {
        const formattedColumn = getFormattedWidgetColumn(widget.content.columnSelectedOfDataset[i], columnNameIdMap)

        if (allowDuplicates) {
            formattedColumns.push(formattedColumn)
        } else {
            const index = formattedColumns.findIndex((column: IWidgetColumn) => column.columnName === formattedColumn.columnName && column.alias === formattedColumn.alias)
            if (index === -1) formattedColumns.push(formattedColumn)
        }

    }
    return formattedColumns
}

const getFormattedWidgetColumn = (widgetColumn: any, columnNameIdMap: any) => {
    const formattedColumn = { id: cryptoRandomString({ length: 16, type: 'base64' }), columnName: widgetColumn.name, alias: widgetColumn.alias, type: widgetColumn.type, fieldType: widgetColumn.fieldType, multiValue: widgetColumn.multiValue, filter: {} } as IWidgetColumn
    if (widgetColumn.isCalculated) {
        formattedColumn.formula = widgetColumn.formula
        formattedColumn.formulaEditor = widgetColumn.formulaEditor
    }
    columnNameIdMap[formattedColumn.columnName] = formattedColumn.id
    if (widgetColumn.aggregationSelected) formattedColumn.aggregation = widgetColumn.aggregationSelected
    return formattedColumn
}