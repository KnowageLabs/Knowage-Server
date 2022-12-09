import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";
import { addHighchartsColumnToTable } from "./highcharts/HighchartsDataTabHelpers";


export const addChartColumnToTable = (tempColum: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean, widgetModel: IWidget) => {
    console.log(">>>>>> CHART TYPE: ", chartType)
    if (chartType === 'highchartsPieChart') addHighchartsColumnToTable(tempColum, rows, chartType, attributesOnly, measuresOnly, widgetModel)
}