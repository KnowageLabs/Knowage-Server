import { IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";
import { addHighchartsColumnToTable } from "./highcharts/HighchartsDataTabHelpers";


export const addChartColumnToTable = (tempColum: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean) => {
    if (chartType?.startsWith('highcharts')) addHighchartsColumnToTable(tempColum, rows, chartType, attributesOnly, measuresOnly)
}