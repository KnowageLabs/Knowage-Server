import { IPivotColumnsConfiguration, IPivotFieldPicker, IPivotRowsConfiguration, IWidgetExports } from '../../Dashboard'
import { IPivotTableConfiguration } from '../../interfaces/pivotTable/DashboardPivotTableWidget'

export const getFormattedConfiguration = (widget: any) => {
    const widgetConfig = widget.content.crosstabDefinition.config
    console.log('getFormattedConfiguration', widget)
    return {
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports,
        rows: { grandTotal: widgetConfig.calculatetotalsonrows, subTotal: widgetConfig.calculatesubtotalsonrows, grandTotalLabel: widgetConfig.rowtotalLabel } as IPivotRowsConfiguration,
        columns: { grandTotal: widgetConfig.calculatetotalsoncolumns, subTotal: widgetConfig.calculatesubtotalsoncolumns, grandTotalLabel: widgetConfig.columntotalLabel } as IPivotColumnsConfiguration,
        fieldPicker: { enabled: true, width: 400, height: 400 } as IPivotFieldPicker
    } as IPivotTableConfiguration
}
