import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";

export const addVegaColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean) => {
    let mode = ''
    if (attributesOnly) mode = 'attributesOnly'
    else if (measuresOnly) mode = 'measuresOnly'
    switch (chartType) {
        case 'wordcloud':
            addWordcloudColumnToTable(tempColumn, rows, chartType, mode)
    }
}

const addWordcloudColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, mode: string) => {
    if (mode === 'attributesOnly' && rows.length <= 1) {
        if (tempColumn.fieldType === 'MEASURE') {
            tempColumn.fieldType = 'ATTRIBUTE'
            tempColumn.aggregation = ''
        }
        rows[0] = tempColumn
    } else if (mode === 'measuresOnly' && rows.length <= 1) {
        if (tempColumn.fieldType === 'ATTRIBUTE') {
            tempColumn.fieldType = 'MEASURE'
            tempColumn.aggregation = 'SUM'
        }
        rows[0] = tempColumn
    }
}

export const updateWidgetModelColumnsAfterChartTypeChange = (widget: IWidget, chartType: string) => {
    const maxAttributeColumns = chartType === 'wordcloud' ? 1 : 0
    const maxMeasureColumns = getMaxValuesNumber(chartType) ?? widget.columns.length
    const updatedColumns = [] as IWidgetColumn[]
    let attributesAdded = 0
    let measuresAdded = 0
    widget.columns.forEach((column: IWidgetColumn) => {
        if (column.fieldType === 'ATTRIBUTE' && attributesAdded < maxAttributeColumns) {
            updatedColumns.push(column)
            attributesAdded++
        } else if (column.fieldType === 'MEASURE' && measuresAdded < maxMeasureColumns) {
            updatedColumns.push(column)
            measuresAdded++
        }
    })
    widget.columns = updatedColumns
}

const getMaxValuesNumber = (chartType: string | undefined) => {
    switch (chartType) {
        case 'wordcloud':
            return 1;
        default:
            return null
    }
}

