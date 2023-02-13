import { IPivotTableConfiguration } from "../../interfaces/pivotTable/DashboardPivotTableWidget"
import { getFormattedExport } from "../tableWidget/TableWidgetConfigurationHelper"

export const getFormattedConfiguration = (widget: any) => {
    return { exports: getFormattedExport(widget) } as IPivotTableConfiguration
}