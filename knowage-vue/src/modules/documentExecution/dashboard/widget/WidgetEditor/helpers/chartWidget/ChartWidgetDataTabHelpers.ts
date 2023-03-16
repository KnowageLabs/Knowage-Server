import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";
import { addChartJSColumnToTable } from "./chartJS/ChartJSDataTableHelpers";
import { addHighchartsColumnToTable } from "./highcharts/HighchartsDataTabHelpers";
import useStore from '@/App.store'


export const addChartColumnToTable = (tempColum: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean, widgetModel: IWidget) => {
    const store = useStore()
    store.user.enterprise ? addHighchartsColumnToTable(tempColum, rows, chartType, attributesOnly, measuresOnly, widgetModel) : addChartJSColumnToTable(tempColum, rows, chartType, attributesOnly, measuresOnly, widgetModel)
}