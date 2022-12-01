import { IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";
import deepcopy from "deepcopy";

export const addHighchartsColumnToTable = (tempColum: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean) => {
    let mode = ''
    if (attributesOnly) mode = 'attributesOnly'
    else if (measuresOnly) mode = 'measuresOnly'
    switch (chartType) {
        case 'highchartsPieChart':
            addHighchartsPieChartColumnToTable(tempColum, rows, mode)
    }
}

const addHighchartsPieChartColumnToTable = (tempColum: IWidgetColumn, rows: IWidgetColumn[], mode: string) => {
    if (mode === 'attributesOnly' && tempColum.fieldType === 'ATTRIBUTE' && rows.length < 4) {
        const index = rows.findIndex((column: IWidgetColumn) => column.columnName === tempColum.columnName)
        if (index === -1) rows.push(tempColum)
    } else if (mode === 'measuresOnly' && tempColum.fieldType === 'MEASURE' && rows.length <= 1) {
        rows.length === 1 ? rows[0] = tempColum : rows.push(tempColum)
    }
}