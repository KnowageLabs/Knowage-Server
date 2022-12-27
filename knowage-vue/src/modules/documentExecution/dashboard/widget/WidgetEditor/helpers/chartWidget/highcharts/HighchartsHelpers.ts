import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { KnowageHighchartsPieChart } from "../../../../ChartWidget/classes/highcharts/KnowageHighchartsPieChart"
import { IHighchartsWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import { KnowageHighchartsGaugeChart } from "../../../../ChartWidget/classes/highcharts/KnowageHighchartsGaugeChart"
import * as widgetCommonDefaultValues from '../../common/WidgetCommonDefaultValues'
import * as  highchartsDefaultValues from "../highcharts/HighchartsDefaultValues"
import descriptor from '../../../WidgetEditorSettingsTab/ChartWidget/common/ChartColorSettingsDescriptor.json'
import { KnowageHighchartsActivityGaugeChart } from "../../../../ChartWidget/classes/highcharts/KnowageHighchartsActivityGaugeChart"

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
            crosssNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
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
    console.log(">>>>>>> CHART TYPE: ", chartType)
    switch (chartType) {
        case 'pie':
            widget.settings.chartModel = new KnowageHighchartsPieChart(chartModel)
            break
        case 'gauge':
            widget.settings.chartModel = new KnowageHighchartsGaugeChart(chartModel)
            break
        case 'activitygauge':
            widget.settings.chartModel = new KnowageHighchartsActivityGaugeChart(chartModel)
            break
    }

}





export const createNewHighchartsModel = (chartType: string) => {
    switch (chartType) {
        case 'pie':
            return new KnowageHighchartsPieChart(null)
        case 'gauge':
            return new KnowageHighchartsGaugeChart(null)
        case 'solidgauge':
            return new KnowageHighchartsActivityGaugeChart(null)
        default:
            return null
    }
}

const getSeriesAccesibilitySettings = () => {
    return [{ names: [], accessibility: highchartsDefaultValues.getDefaultSeriesAccessibilitySettings() }]
}


const getSerieLabelsSettings = () => {
    const serieLabelSettings = { names: [], label: highchartsDefaultValues.getDefaultSerieLabelSettings(), dial: highchartsDefaultValues.getDefaultSerieDialSettings(), pivot: highchartsDefaultValues.getDefaultSeriePivotSettings() }
    return [serieLabelSettings]
}