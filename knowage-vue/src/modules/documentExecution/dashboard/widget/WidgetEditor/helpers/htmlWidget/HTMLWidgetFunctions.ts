import { IHTMLWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/DashboardHTMLWidget"
import * as widgetCommonDefaultValues from '../common/WidgetCommonDefaultValues'

export const createNewHtmlWidgetSettings = () => {
    return {
        sortingColumn: '',
        sortingOrder: '',
        updatable: true,
        clickable: true,
        editor: { css: '', html: '' },
        configuration: {
            exports: { showExcelExport: true, showScreenshot: true }
        },
        interactions: {
            crosssNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
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
    } as IHTMLWidgetSettings
}
