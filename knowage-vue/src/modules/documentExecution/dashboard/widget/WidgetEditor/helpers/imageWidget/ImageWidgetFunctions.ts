
import { IImageWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/DashboardImageWidget"
import * as imageWidgetDefaultValues from './ImageWidgetDefaultValues'
import * as widgetCommonDefaultValues from '../common/WidgetCommonDefaultValues'

export const createNewImageWidgetSettings = () => {
    return {
        updatable: true,
        clickable: true,
        configuration: {
            image: { id: -1, style: imageWidgetDefaultValues.getdefaultImageStyleSettings() },
            exports: { showExcelExport: true, showScreenshot: true }
        },
        interactions: {
            crossNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
        },
        style: {
            title: widgetCommonDefaultValues.getDefaultTitleStyle(),
            padding: widgetCommonDefaultValues.getDefaultPaddingStyle(),
            borders: widgetCommonDefaultValues.getDefaultBordersStyle(),
            shadows: widgetCommonDefaultValues.getDefaultShadowsStyle(),
            background: widgetCommonDefaultValues.getDefaultBackgroundStyle()
        },
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IImageWidgetSettings
}
