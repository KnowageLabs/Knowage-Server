import { KnowageVegaChartWordcloud } from '../../../widget/ChartWidget/classes/vega/KnowageVegaChartWordcloud';
import { getFormattedStyle } from './VegaChartsStyleHelper';
import { getFormattedInteractions } from './../../common/WidgetInteractionsHelper';
import { getFormattedColorSettings, getFormattedWidgetColumns } from './../CommonChartCompatibilityHelper';
import { IWidget, IWidgetExports } from './../../../Dashboard.d';
import { getFiltersForColumns } from './../../DashboardBackwardCompatibilityHelper';
import { IVegaChartsSettings, IVegaChartsConfiguration } from './../../../interfaces/vega/VegaChartsWidget.d';
import * as widgetCommonDefaultValues from '../../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'

export const formatVegaChartsWidget = (widget: any) => {
    console.log('------------- ORIGINAL WIDGET: ', widget)

    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ?? null,
        type: 'vega',
        columns: getFormattedWidgetColumns(widget, 'vega'),
        theme: '',
        settings: {} as IVegaChartsSettings
    } as IWidget

    formattedWidget.settings = getFormattedWidgetSettings(widget) as IVegaChartsSettings
    getFiltersForColumns(formattedWidget, widget)
    formattedWidget.settings.chartModel = createChartModel(widget)

    console.log('------------- FORMATTED WIDGET: ', formattedWidget)

    return formattedWidget
}


const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        chartModel: null,
        configuration: getFormattedConfiguration(widget),
        interactions: getFormattedInteractions(widget),
        style: getFormattedStyle(widget),
        chart: { colors: getFormattedColorSettings(widget) },
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IVegaChartsSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return { exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports } as IVegaChartsConfiguration
}


const createChartModel = (widget: any) => {
    const widgetContentChartTemplate = widget.content.chartTemplate
    switch (widgetContentChartTemplate.CHART.type) {
        case "WORDCLOUD":
            return new KnowageVegaChartWordcloud(widgetContentChartTemplate)
        default:
            return null
    }
}