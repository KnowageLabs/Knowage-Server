import { IWidget, IWidgetExports, IWidgetInteractions } from "../../../Dashboard"
import { HighchartsChartModel, IHighchartsWidgetConfiguration, IHighchartsWidgetSettings } from "../../../interfaces/highcharts/DashboardHighchartsWidget"
import { getFormattedWidgetColumns } from "../../common/WidgetColumnHelper"
import * as widgetCommonDefaultValues from '../../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFormattedInteractions } from "../../common/WidgetInteractionsHelper"
import { getFormattedStyle } from "./HighchartsWidgetStyleHelper"

const columnNameIdMap = {}

export const formatHighchartsWidget = (widget: any) => {
    console.log(">>>>>>>>>>> OLD WIDGET: ", widget)
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ?? null,
        type: widget.type,
        columns: getFormattedWidgetColumns(widget, columnNameIdMap),
        theme: '',
        settings: {} as IHighchartsWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget) as IHighchartsWidgetSettings
    console.log(">>>>>>>>>>> FORMATTED WIDGET: ", widget)
    return formattedWidget
}


const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        chartModel: {} as HighchartsChartModel, // TODO
        configuration: getFormattedConfiguration(widget),
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        style: getFormattedStyle(widget),
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IHighchartsWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return {
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    } as IHighchartsWidgetConfiguration
}