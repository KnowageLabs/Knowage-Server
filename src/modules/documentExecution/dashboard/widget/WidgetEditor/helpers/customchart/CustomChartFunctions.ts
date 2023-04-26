import { ICustomChartWidgetEditor, ICustomChartWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/customChart/DashboardCustomChartWidget"
import * as widgetCommonDefaultValues from '../common/WidgetCommonDefaultValues'

export const createNewCustomChartSettings = () => {
    return {
        updatable: true,
        clickable: true,
        editor: { css: '', html: '', js: '' } as ICustomChartWidgetEditor,
        configuration: { exports: { showExcelExport: true, showScreenshot: true } },
        interactions: {
            crossNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
            preview: widgetCommonDefaultValues.getDefaultPreview(),
        },
        style: {
            title: widgetCommonDefaultValues.getDefaultTitleStyle(),
            padding: widgetCommonDefaultValues.getDefaultPaddingStyle(),
            borders: widgetCommonDefaultValues.getDefaultBordersStyle(),
            shadows: widgetCommonDefaultValues.getDefaultShadowsStyle(),
            background: widgetCommonDefaultValues.getDefaultBackgroundStyle()
        },
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as ICustomChartWidgetSettings
}
