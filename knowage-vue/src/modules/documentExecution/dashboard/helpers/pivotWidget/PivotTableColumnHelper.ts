import { IWidgetColumn } from '../../Dashboard'
import { IPivotFields } from '../../interfaces/pivotTable/DashboardPivotTableWidget'
import cryptoRandomString from 'crypto-random-string'

export const getFormattedPivotFields = (widget: any, columnNameIdMap: any) => {
    if (!widget.content || !widget.content.crosstabDefinition) return []

    const formattedColumns = {
        columns: getFormattedFields(widget.content.crosstabDefinition.columns, columnNameIdMap),
        rows: getFormattedFields(widget.content.crosstabDefinition.rows, columnNameIdMap),
        data: getFormattedFields(widget.content.crosstabDefinition.measures, columnNameIdMap),
        filters: []
    } as IPivotFields

    return formattedColumns
}

export const getFormattedFields = (widgetColumns: any, columnNameIdMap: any) => {
    const formattedColumns = [] as IWidgetColumn[]

    widgetColumns.forEach((widgetColumn) => {
        const formattedColumn = { id: cryptoRandomString({ length: 16, type: 'base64' }), columnName: widgetColumn.id, alias: widgetColumn.alias, type: 'TODO - no type in old model', fieldType: widgetColumn.nature.toUpperCase(), filter: {} } as IWidgetColumn

        if (widgetColumn.isCalculated) {
            formattedColumn.formula = widgetColumn.formula
            formattedColumn.formulaEditor = widgetColumn.formulaEditor
        }
        columnNameIdMap[formattedColumn.columnName] = formattedColumn.id
        if (widgetColumn.funct) formattedColumn.aggregation = widgetColumn.funct

        formattedColumns.push(formattedColumn)
    })

    return formattedColumns
}
