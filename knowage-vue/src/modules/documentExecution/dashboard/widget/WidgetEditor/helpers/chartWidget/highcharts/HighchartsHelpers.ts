import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { KnowageHighchartsPieChart } from "../../../../ChartWidget/classes/highcharts/KnowageHighchartsPieChart"
import { IHighchartsChartModel, IHighchartsWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import { KnowageHighchartsActivityGaugeChart } from "../../../../ChartWidget/classes/highcharts/KnowageHighchartsActivityGaugeChart"
import { KnowageHighchartsSolidGaugeChart } from "../../../../ChartWidget/classes/highcharts/KnowageHighchartsSolidGaugeChart"
import { KnowageHighchartsGaugeSeriesChart } from "../../../../ChartWidget/classes/highcharts/KnowaageHighchartsGaugeSeriesChart"
import * as widgetCommonDefaultValues from '../../common/WidgetCommonDefaultValues'
import * as  highchartsDefaultValues from "../highcharts/HighchartsDefaultValues"
import descriptor from '../../../WidgetEditorSettingsTab/ChartWidget/common/ChartColorSettingsDescriptor.json'

export const createNewHighchartsSettings = () => {
    const settings = {
        updatable: true,
        clickable: true,
        chartModel: null,
        configuration: { exports: { showExcelExport: true, showScreenshot: true } },
        accesssibility: { seriesAccesibilitySettings: getSeriesAccesibilitySettings() },
        series: { seriesLabelsSettings: getSerieLabelsSettings() },
        interactions: {
            drilldown: { enabled: false },
            crossNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
            link: widgetCommonDefaultValues.getDefaultLinks(),
            preview: widgetCommonDefaultValues.getDefaultPreview(),
            selection: highchartsDefaultValues.getDefaultHighchartsSelections(),
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
    } as IHighchartsWidgetSettings
    settings.chartModel = null
    return settings
}

export const formatHighchartsWidget = (widget: IWidget) => {
    const chartModel = widget.settings.chartModel.model ?? widget.settings.chartModel
    const chartType = chartModel.chart.type
    switch (chartType) {
        case 'pie':
            widget.settings.chartModel = new KnowageHighchartsPieChart(chartModel)
            break
        case 'gauge':
            widget.settings.chartModel = new KnowageHighchartsGaugeSeriesChart(chartModel)
            break
        case 'activitygauge':
            widget.settings.chartModel = new KnowageHighchartsActivityGaugeChart(chartModel)
            break
        case 'solidgauge':
            widget.settings.chartModel = new KnowageHighchartsSolidGaugeChart(chartModel)
            break
    }

}

export const createNewHighchartsModel = (chartType: string, model: IHighchartsChartModel | null = null) => {
    switch (chartType) {
        case 'pie':
            return new KnowageHighchartsPieChart(model)
        case 'gauge':
            return new KnowageHighchartsGaugeSeriesChart(model)
        case 'activitygauge':
            return new KnowageHighchartsActivityGaugeChart(model)
        case 'solidgauge':
            return new KnowageHighchartsSolidGaugeChart(model)
        default:
            return null
    }
}

const getSeriesAccesibilitySettings = () => {
    return [{ names: ['all'], accessibility: highchartsDefaultValues.getDefaultSeriesAccessibilitySettings() }]
}


const getSerieLabelsSettings = () => {
    const serieLabelSettings = { names: ['all'], label: { ...highchartsDefaultValues.getDefaultSerieLabelSettings(), enabled: true }, dial: highchartsDefaultValues.getDefaultSerieDialSettings(), pivot: highchartsDefaultValues.getDefaultSeriePivotSettings() }
    return [serieLabelSettings]
}
