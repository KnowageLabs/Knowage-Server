import { IWidget, IWidgetExports, IWidgetInteractions } from '../../../Dashboard'
import { IHighchartsSeriesLabelsSetting, IHighchartsWidgetConfiguration, IHighchartsWidgetSettings } from '../../../interfaces/highcharts/DashboardHighchartsWidget'
import { IHighchartsPieChart } from '../../../widget/ChartWidget/classes/highcharts/KnowageIHighchartsPieChart'
import { getFormattedInteractions } from '../../common/WidgetInteractionsHelper'
import { getFiltersForColumns } from '../../DashboardBackwardCompatibilityHelper'
import { hexToRgba } from '../../FormattingHelpers'
import { getFormattedWidgetColumns, getFormattedColorSettings } from '../CommonChartCompatibilityHelper'
import { getFormattedStyle } from './HighchartsWidgetStyleHelper'
import * as widgetCommonDefaultValues from '../../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as highchartsDefaultValues from '../../../widget/WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'

const columnNameIdMap = {}

export const formatHighchartsWidget = (widget: any) => {
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ?? null,
        type: 'highcharts',
        columns: getFormattedWidgetColumns(widget, 'highcharts'),
        theme: '',
        settings: {} as IHighchartsWidgetSettings
    } as IWidget

    formattedWidget.settings = getFormattedWidgetSettings(widget) as IHighchartsWidgetSettings
    getFiltersForColumns(formattedWidget, widget)
    formattedWidget.settings.chartModel = createChartModel(widget)
    return formattedWidget
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        chartModel: null,
        configuration: getFormattedConfiguration(widget),
        accesssibility: { seriesAccesibilitySettings: getFormattedSeriesAccesibilitySettings(widget) },
        series: { seriesLabelsSettings: getFormattedSerieLabelsSettings(widget) },
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        style: getFormattedStyle(widget),
        chart: { colors: getFormattedColorSettings(widget) as any },
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IHighchartsWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return {
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    } as IHighchartsWidgetConfiguration
}

const getFormattedSeriesAccesibilitySettings = (widget: any) => {
    return widget.content.chartTemplate.CHART.type !== 'PIE' ? highchartsDefaultValues.getDefaultAllSeriesAccessibilitySettings() : []
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}

const createChartModel = (widget: any) => {
    switch (widget.content.chartTemplate.CHART.type) {
        case 'PIE':
            return new IHighchartsPieChart(widget.content.chartTemplate)
        default:
            return null
    }
}

const getFormattedSerieLabelsSettings = (widget: any) => {
    const formattedSerieSettings =
        widget.content.chartTemplate.CHART.type !== 'PIE' ? highchartsDefaultValues.getDefaultSerieLabelSettings() : ([] as IHighchartsSeriesLabelsSetting[])
    if (widget.content.chartTemplate.CHART.VALUES.SERIE && widget.content.chartTemplate.CHART.VALUES.SERIE[0]) {
        const oldModelSerie = widget.content.chartTemplate.CHART.VALUES.SERIE[0]
        formattedSerieSettings.push({
            names: [oldModelSerie.name],
            label: {
                enabled: true,
                style: {
                    fontFamily: oldModelSerie.dataLabels.style.fontFamily ?? '',
                    fontSize: oldModelSerie.dataLabels.style.fontSize ?? '',
                    fontWeight: oldModelSerie.dataLabels.style.fontWeight ?? '',
                    color: oldModelSerie.dataLabels.style.color ? hexToRgba(oldModelSerie.dataLabels.style.color) : '',
                },
                backgroundColor: '',
                prefix: oldModelSerie.prefixChar ?? '',
                suffix: oldModelSerie.postfixChar ?? '',
                scale: oldModelSerie.scaleFactor ?? 'empty', // TODO
                precision: oldModelSerie.precision ?? 2,
                absolute: oldModelSerie.showAbsValue,
                percentage: oldModelSerie.showPercentage
            }
        })
    }
    return formattedSerieSettings
}
