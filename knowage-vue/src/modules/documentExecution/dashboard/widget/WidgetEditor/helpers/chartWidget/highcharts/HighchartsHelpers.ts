import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { HighchartsPieChart } from "../../../../ChartWidget/classes/highcharts/KnowageHighchartsPieChart"
import { IHighchartsWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import * as widgetCommonDefaultValues from '../../common/WidgetCommonDefaultValues'
import * as  highchartsDefaultValues from "../highcharts/HighchartsDefaultValues"
import descriptor from '../../../WidgetEditorSettingsTab/ChartWidget/common/ChartColorSettingsDescriptor.json'

export const createNewHighchartsSettings = () => {
    const settings = {
        updatable: true,
        clickable: true,
        chartModel: null,
        configuration: { exports: { showExcelExport: true, showScreenshot: true } },
        accesssibility: { seriesAccesibilitySettings: [] },
        series: { seriesLabelsSettings: [] },
        interactions: {
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
    widget.settings.chartModel = new HighchartsPieChart(widget.settings.chartModel)

}


export const createNewHighchartsModel = (chartType: string) => {
    switch (chartType) {
        case 'pie':
            return new HighchartsPieChart(null)
        default:
            return null
    }
}