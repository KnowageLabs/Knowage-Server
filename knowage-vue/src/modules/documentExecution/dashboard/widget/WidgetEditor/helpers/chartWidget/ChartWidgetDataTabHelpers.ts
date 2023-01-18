import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";
import { addChartJSColumnToTable } from "./chartJS/ChartJSDataTableHelpers";
import { addHighchartsColumnToTable } from "./highcharts/HighchartsDataTabHelpers";


export const addChartColumnToTable = (tempColum: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean, widgetModel: IWidget) => {
    // TODO - add user check/lib change
    true ? addHighchartsColumnToTable(tempColum, rows, chartType, attributesOnly, measuresOnly, widgetModel) : addChartJSColumnToTable(tempColum, rows, chartType, attributesOnly, measuresOnly, widgetModel)
}