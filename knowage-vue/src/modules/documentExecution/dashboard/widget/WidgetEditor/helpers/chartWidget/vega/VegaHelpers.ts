import { IVegaChartsSettings } from './../../../../../interfaces/vega/VegaChartsWidget.d';
import { IWidget } from './../../../../../Dashboard.d';
import { KnowageVegaChartWordcloud } from './../../../../ChartWidget/classes/vega/KnowageVegaChartWordcloud';
import * as widgetCommonDefaultValues from '../../common/WidgetCommonDefaultValues'
import * as vegaChartsDefaultValues from '../vega/VegaDefaultValues'
import descriptor from '../../../WidgetEditorSettingsTab/ChartWidget/common/ChartColorSettingsDescriptor.json'

export const createNewVegaSettings = () => {
    const settings = {
        updatable: true,
        clickable: true,
        chartModel: null,
        configuration: {
            textConfiguration: vegaChartsDefaultValues.getDefaultVegaTextConfiguration(),
            noDataConfiguration: vegaChartsDefaultValues.getDefaultVegaNoDataConfiguration(),
            exports: { showExcelExport: true, showScreenshot: true }
        },
        interactions: {
            crossNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
            selection: { enabled: true }
        },
        style: {
            title: widgetCommonDefaultValues.getDefaultTitleStyle(),
            padding: widgetCommonDefaultValues.getDefaultPaddingStyle(),
            borders: widgetCommonDefaultValues.getDefaultBordersStyle(),
            shadows: widgetCommonDefaultValues.getDefaultShadowsStyle(),
            background: widgetCommonDefaultValues.getDefaultBackgroundStyle()
        },
        tooltip: vegaChartsDefaultValues.getDefaultTooltipSettings(),
        chart: { colors: descriptor.defaultColors },
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IVegaChartsSettings
    settings.chartModel = null
    return settings
}

export const formatVegaWidget = (widget: IWidget) => {
    widget.settings.chartModel = new KnowageVegaChartWordcloud(widget.settings.chartModel.model ?? widget.settings.chartModel)
}


export const createVegaModel = (widget: IWidget, chartType: string) => {
    console.log('--------- CHART TYPE: ', chartType)
    widget.type = 'vega'
    switch (chartType) {
        case 'wordcloud':
            return new KnowageVegaChartWordcloud(null)
        default:
            return null
    }
}
