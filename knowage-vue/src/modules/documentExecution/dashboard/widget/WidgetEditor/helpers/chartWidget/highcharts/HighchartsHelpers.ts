import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { KnowageHighchartsPieChart } from "../../../../ChartWidget/classes/highcharts/KnowageHighchartsPieChart"
import { IHighchartsWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import * as widgetCommonDefaultValues from '../../common/WidgetCommonDefaultValues'
import * as  highchartsDefaultValues from "../highcharts/HighchartsDefaultValues"
import descriptor from '../../../WidgetEditorSettingsTab/ChartWidget/common/ChartColorSettingsDescriptor.json'
import { KnowageHighchartsGaugeChart } from "../../../../ChartWidget/classes/highcharts/KnowageHighchartsGaugeChart"

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
    widget.settings.chartModel = chartType === 'pie' ? new KnowageHighchartsPieChart(chartModel) : new KnowageHighchartsGaugeChart(chartModel)

}


export const createNewHighchartsModel = (chartType: string) => {
    switch (chartType) {
        case 'pie':
            return new KnowageHighchartsPieChart(null)
        default:
            return null
    }
}

const getSeriesAccesibilitySettings = () => {
    return [
        {
            names: [],
            accessibility: {
                enabled: false,
                description: '',
                exposeAsGroupOnly: false,
                keyboardNavigation: { enabled: false }
            }
        }
    ]
}


const getSerieLabelsSettings = () => {
    return [
        {
            names: [],
            label: {
                enabled: false,
                style: {
                    fontFamily: '',
                    fontSize: '',
                    fontWeight: '',
                    color: '',
                },
                backgroundColor: '',
                prefix: '',
                suffix: '',
                scale: 'empty',
                precision: 2,
                absolute: false,
                percentage: false
            }
        }
    ]
}
