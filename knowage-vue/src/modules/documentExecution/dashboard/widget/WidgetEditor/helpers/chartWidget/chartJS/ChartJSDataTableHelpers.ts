import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";

export const addChartJSColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean, widgetModel: IWidget) => {
    let mode = ''
    if (attributesOnly) mode = 'attributesOnly'
    else if (measuresOnly) mode = 'measuresOnly'
    switch (chartType) {
        case 'chartJSPieChart':
            addChartJSPieChartColumnToTable(tempColumn, rows, chartType, mode, widgetModel)
    }
}

const addChartJSPieChartColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, mode: string, widgetModel: IWidget) => {
    console.log('----- add column: ', tempColumn)
    console.log('----- add column mode: ', mode)
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

