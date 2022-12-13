import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { IChartJSWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget"
import { ChartJSPieChart } from "../../../../ChartWidget/classes/chartJS/KnowageChartJSPieChart"
import * as widgetCommonDefaultValues from '../../common/WidgetCommonDefaultValues'
import * as  chartJSDefaultValues from "../chartJS/ChartJSDefaultValues"
import descriptor from '../../../WidgetEditorSettingsTab/ChartWidget/common/ChartColorSettingsDescriptor.json'

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
            selection: chartJSDefaultValues.getDefaultChartJSSelections(),
        },
        chart: { colors: descriptor.defaultColors },
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

