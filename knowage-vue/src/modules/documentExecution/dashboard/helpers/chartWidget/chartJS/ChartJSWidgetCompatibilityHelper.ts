import { IWidget, IWidgetColumn, IWidgetExports, IWidgetInteractions } from "../../../Dashboard"
import { IChartJSWidgetConfiguration, IChartJSWidgetSettings } from "../../../interfaces/chartJS/DashboardChartJSWidget"
import { getFormattedInteractions } from "../../common/WidgetInteractionsHelper"
import { getFormattedStyle } from "./ChartJSWidgetStyleHelper"
import { ChartJSPieChart } from "../../../widget/ChartWidget/classes/chartJS/KnowageChartJSPieChart"
import * as widgetCommonDefaultValues from '../../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFormattedWidgetColumn } from "../../common/WidgetColumnHelper"

const columnNameIdMap = {}

export const formatChartJSWidget = (widget: any) => {
    console.log(">>>>>>>>>>> OLD WIDGET: ", widget)
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ?? null,
        type: 'chartJS',
        columns: getFormattedWidgetColumns(widget),
        theme: '',
        settings: {} as IChartJSWidgetSettings
    } as IWidget

    formattedWidget.settings = getFormattedWidgetSettings(widget) as IChartJSWidgetSettings
    //  getFiltersForColumns(formattedWidget, widget)
    formattedWidget.settings.chartModel = createChartModel(widget, formattedWidget)
    console.log(">>>>>>>>>>> FORMATTED WIDGET: ", widget)
    return formattedWidget
}

export const getFormattedWidgetColumns = (widget: any) => {
    if (!widget.content || !widget.content.columnSelectedOfDatasetAggregations || !widget.content.chartTemplate || !widget.content.chartTemplate.CHART || !widget.content.chartTemplate.CHART.VALUES) return []
    const widgetColumNameMap = {}
    for (let i = 0; i < widget.content.columnSelectedOfDatasetAggregations.length; i++) {
        if (!widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name]) widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name] = getFormattedWidgetColumn(widget.content.columnSelectedOfDatasetAggregations[i], columnNameIdMap)
    }

    const formattedColumns = [] as IWidgetColumn[]
    const category = widget.content.chartTemplate.CHART.VALUES.CATEGORY
    const serie = widget.content.chartTemplate.CHART.VALUES.SERIE ? widget.content.chartTemplate.CHART.VALUES.SERIE[0] : null
    // if (category) addCategoryColumns(category, formattedColumns, widgetColumNameMap)
    //if (serie) addSerieColumn(serie, widgetColumNameMap, formattedColumns)
    return formattedColumns
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        chartModel: null,
        configuration: getFormattedConfiguration(widget),
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
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