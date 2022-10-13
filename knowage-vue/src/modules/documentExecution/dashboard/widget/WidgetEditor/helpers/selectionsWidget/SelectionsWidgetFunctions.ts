import { ISelectionsWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/DashboardSelectionsWidget"
import * as selectionsWidgetDefaultValues from './SelectionsWidgetDefaultValues'
import * as widgetCommonDefaultValues from '../common/WidgetCommonDefaultValues'

export const createNewSelectionsWidgetSettings = () => {
    return {
        updatable: true,
        clickable: true,
        configuration: {
            type: "list",
            valuesManagement: selectionsWidgetDefaultValues.getDefaultValuesManagement(),
            noSelections: selectionsWidgetDefaultValues.getDefaultNoSelectionsConfiguration(),
            exports: { showExcelExport: true }
        },
        style: {
            title: widgetCommonDefaultValues.getDefaultTitleStyle(),
            chips: selectionsWidgetDefaultValues.getDefaultChipsStyle(),
            rows: selectionsWidgetDefaultValues.getDefaultRowsStyle(),
            padding: widgetCommonDefaultValues.getDefaultPaddingStyle(),
            borders: widgetCommonDefaultValues.getDefaultBordersStyle(),
            shadows: widgetCommonDefaultValues.getDefaultShadowsStyle(),
            background: selectionsWidgetDefaultValues.getDefaultBackgroundStyle()
        },
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as ISelectionsWidgetSettings
}
