import { IWidget, IWidgetExports, IWidgetInteractions } from '../../../Dashboard'
import { IHighchartsWidgetConfiguration, IHighchartsWidgetSettings } from '../../../interfaces/highcharts/DashboardHighchartsWidget'
import { KnowageHighchartsPieChart } from '../../../widget/ChartWidget/classes/highcharts/KnowageHighchartsPieChart'
import { getFormattedInteractions } from '../../common/WidgetInteractionsHelper'
import { getFiltersForColumns } from '../../DashboardBackwardCompatibilityHelper'
import { getFormattedWidgetColumns, getFormattedColorSettings } from '../CommonChartCompatibilityHelper'
import { getFormattedStyle } from './HighchartsWidgetStyleHelper'
import { KnowageHighchartsGaugeSeriesChart } from '../../../widget/ChartWidget/classes/highcharts/KnowaageHighchartsGaugeSeriesChart'
import { KnowageHighchartsSolidGaugeChart } from '../../../widget/ChartWidget/classes/highcharts/KnowageHighchartsSolidGaugeChart'
import { KnowageHighchartsActivityGaugeChart } from '../../../widget/ChartWidget/classes/highcharts/KnowageHighchartsActivityGaugeChart'
import { getFormattedSerieLabelsSettings } from './HighchartsSeriesSettingsCompatibilityHelper'
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
    const widgetContentChartTemplate = widget.content.chartTemplate
    switch (widgetContentChartTemplate.CHART.type) {
        case 'PIE':
            return new KnowageHighchartsPieChart(widgetContentChartTemplate)
        case 'GAUGE':
            return createGaugeChartInstance(widgetContentChartTemplate)
        default:
            return null
    }
}

const createGaugeChartInstance = (widgetContentChartTemplate: any) => {
    switch (widgetContentChartTemplate.CHART.subtype) {
        case 'activity':
            return new KnowageHighchartsActivityGaugeChart(widgetContentChartTemplate)
        case 'solid':
            return new KnowageHighchartsSolidGaugeChart(widgetContentChartTemplate)
        case 'simple':
        default:
            return new KnowageHighchartsGaugeSeriesChart(widgetContentChartTemplate)

    }
}