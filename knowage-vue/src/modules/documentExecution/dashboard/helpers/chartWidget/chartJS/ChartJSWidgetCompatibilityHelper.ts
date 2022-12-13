import { IWidget, IWidgetExports, IWidgetInteractions } from "../../../Dashboard"
import { IChartJSWidgetConfiguration, IChartJSWidgetSettings } from "../../../interfaces/chartJS/DashboardChartJSWidget"
import { getFormattedInteractions } from "../../common/WidgetInteractionsHelper"
import { getFormattedStyle } from "./ChartJSWidgetStyleHelper"
import { ChartJSPieChart } from "../../../widget/ChartWidget/classes/chartJS/KnowageChartJSPieChart"
import * as widgetCommonDefaultValues from '../../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFormattedColorSettings, getFormattedWidgetColumns } from "../CommonChartCompatibilityHelper"
import { getFiltersForColumns } from "../../DashboardBackwardCompatibilityHelper"


export const formatChartJSWidget = (widget: any) => {
    console.log(">>>>>>>>>>> OLD WIDGET: ", widget)
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ?? null,
        type: 'chartJS',
        columns: getFormattedWidgetColumns(widget, 'chartJS'),
        theme: '',
        settings: {} as IChartJSWidgetSettings
    } as IWidget

    formattedWidget.settings = getFormattedWidgetSettings(widget) as IChartJSWidgetSettings
    getFiltersForColumns(formattedWidget, widget)
    formattedWidget.settings.chartModel = createChartModel(widget, formattedWidget)
    console.log(">>>>>>>>>>> FORMATTED WIDGET: ", formattedWidget)
    return formattedWidget
}


const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        chartModel: null,
        configuration: getFormattedConfiguration(widget),
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        chart: {
            colors: getFormattedColorSettings(widget)
        },
        style: getFormattedStyle(widget),
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IChartJSWidgetSettings
    return formattedSettings
}


const getFormattedConfiguration = (widget: any) => {
    return {
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    } as IChartJSWidgetConfiguration
}


const createChartModel = (widget: any, formattedWidget: IWidget) => {
    switch (widget.content.chartTemplate.CHART.type) {
        case 'PIE':
            return new ChartJSPieChart(widget.content.chartTemplate, formattedWidget)
        default:
            return null
    }
}