import { IWidgetExports } from "../../Dashboard"
import { IPivotTableConfiguration } from "../../interfaces/pivotTable/DashboardPivotTableWidget"

export const getFormattedConfiguration = (widget: any) => {
    return { exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports } as IPivotTableConfiguration
}