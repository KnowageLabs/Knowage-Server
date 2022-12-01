import { IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";

export const addHighchartsColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean) => {
    let mode = ''
    if (attributesOnly) mode = 'attributesOnly'
    else if (measuresOnly) mode = 'measuresOnly'
    switch (chartType) {
        case 'highchartsPieChart':
            addHighchartsPieChartColumnToTable(tempColumn, rows, mode)
    }
}

const addHighchartsPieChartColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], mode: string) => {
    if (mode === 'attributesOnly' && rows.length < 4) {
        const index = rows.findIndex((column: IWidgetColumn) => column.columnName === tempColumn.columnName)
        if (tempColumn.fieldType === 'MEASURE') {
            tempColumn.fieldType = 'ATTRIBUTE'
            tempColumn.aggregation = ''
        }
        if (index === -1) rows.push(tempColumn)
    } else if (mode === 'measuresOnly' && rows.length <= 1) {
        if (tempColumn.fieldType === 'ATTRIBUTE') {
            tempColumn.fieldType = 'MEASURE'
            tempColumn.aggregation = 'SUM'
        }
        rows.length === 1 ? rows[0] = tempColumn : rows.push(tempColumn)
    }
}