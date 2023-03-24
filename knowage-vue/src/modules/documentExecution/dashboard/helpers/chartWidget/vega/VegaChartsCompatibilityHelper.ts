import { hexToRgb } from './../../FormattingHelpers';
import { KnowageVegaChartWordcloud } from '../../../widget/ChartWidget/classes/vega/KnowageVegaChartWordcloud';
import { getFormattedStyle } from './VegaChartsStyleHelper';
import { getFormattedInteractions } from './../../common/WidgetInteractionsHelper';
import { getFormattedWidgetColumns } from './../CommonChartCompatibilityHelper';
import { IWidget, IWidgetExports } from './../../../Dashboard.d';
import { getFiltersForColumns } from './../../DashboardBackwardCompatibilityHelper';
import { IVegaChartsSettings, IVegaChartsConfiguration } from './../../../interfaces/vega/VegaChartsWidget.d';
import * as widgetCommonDefaultValues from '../../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as vegaChartsDefaultValues from '../../../widget/WidgetEditor/helpers/chartWidget/vega/VegaDefaultValues'

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
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IVegaChartsSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return {
        noDataConfiguration: getFormattedNoDataConfiguration(widget),
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    } as IVegaChartsConfiguration
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


const getFormattedNoDataConfiguration = (widget: any) => {
    const formmattedNoDataConfiguration = vegaChartsDefaultValues.getDefaultVegaNoDataConfiguration()
    const oldModel = widget.content.chartTemplate
    if (oldModel.CHART.EMPTYMESSAGE) {
        formmattedNoDataConfiguration.text = oldModel.CHART.EMPTYMESSAGE.text
        formmattedNoDataConfiguration.position = oldModel.CHART.EMPTYMESSAGE.position ? { align: oldModel.CHART.EMPTYMESSAGE.position.align, verticalAlign: oldModel.CHART.EMPTYMESSAGE.position.verticalAlign } : { align: formmattedNoDataConfiguration.position.align, verticalAlign: formmattedNoDataConfiguration.position.verticalAlign }

        if (oldModel.CHART.EMPTYMESSAGE.style) {
            formmattedNoDataConfiguration.style = {
                'font-family': oldModel.CHART.EMPTYMESSAGE.style.fontFamily ?? formmattedNoDataConfiguration.style['font-family'],
                'font-size': oldModel.CHART.EMPTYMESSAGE.style.fontSize ?? formmattedNoDataConfiguration.style['font-size'],
                'font-weight': oldModel.CHART.EMPTYMESSAGE.style.fontWeight ?? formmattedNoDataConfiguration.style['font-weight'],
                color: oldModel.CHART.EMPTYMESSAGE.style.color ? hexToRgb(oldModel.CHART.EMPTYMESSAGE.style.color) : formmattedNoDataConfiguration.style.color,
                'background-color': formmattedNoDataConfiguration.style['background-color']
            }
        }
    }
    return formmattedNoDataConfiguration
}

