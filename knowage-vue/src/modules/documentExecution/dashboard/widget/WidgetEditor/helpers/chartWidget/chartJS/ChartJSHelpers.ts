import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { IChartJSWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget"
import { ChartJSPieChart } from "../../../../ChartWidget/classes/chartJS/KnowageChartJSPieChart"
import * as widgetCommonDefaultValues from '../../common/WidgetCommonDefaultValues'

export const createNewChartJSSettings = (widget: IWidget) => {
    const settings = {
        updatable: true,
        clickable: true,
        chartModel: null,
        configuration: { exports: { showExcelExport: true, showScreenshot: true } },
        interactions: {
            crosssNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
            link: widgetCommonDefaultValues.getDefaultLinks(),
            preview: widgetCommonDefaultValues.getDefaultPreview(),
            // selection: tableWidgetDefaultValues.getDefaultSelection(), // TODO
        },
        chart: { colors: [] }, // TODO
        style: {
            title: widgetCommonDefaultValues.getDefaultTitleStyle(),
            padding: widgetCommonDefaultValues.getDefaultPaddingStyle(),
            borders: widgetCommonDefaultValues.getDefaultBordersStyle(),
            shadows: widgetCommonDefaultValues.getDefaultShadowsStyle(),
            background: widgetCommonDefaultValues.getDefaultBackgroundStyle()
        },
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IChartJSWidgetSettings
    settings.chartModel = new ChartJSPieChart(null, widget)  // TODO - see about this (when creating dropdown)

    return settings
}

