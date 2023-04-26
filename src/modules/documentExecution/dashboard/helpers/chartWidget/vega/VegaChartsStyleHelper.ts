import { hexToRgba } from '@/modules/documentExecution/dashboard/helpers/FormattingHelpers';
import { IWidgetBackgroundStyle } from '@/modules/documentExecution/dashboard/Dashboard';
import { IVegaChartsStyle } from './../../../interfaces/vega/VegaChartsWidget.d';
import { getFormattedTitleStyle, getFormattedPaddingStyle, getFormattedBorderStyle, getFormattedShadowsStyle } from '../../common/WidgetStyleHelper'
import * as widgetCommonDefaultValues from '../../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        borders: getFormattedBorderStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        background: getFormattedBackgroundStyle(widget)
    } as IVegaChartsStyle
}

const getFormattedBackgroundStyle = (widget: any) => {
    const widgetContentChartTemplate = widget.content.chartTemplate
    if (!widgetContentChartTemplate || !widgetContentChartTemplate.CHART || !widgetContentChartTemplate.CHART.style) return widgetCommonDefaultValues.getDefaultBackgroundStyle()
    return { enabled: true, properties: { "background-color": widgetContentChartTemplate.CHART.style.backgroundColor ? hexToRgba(widgetContentChartTemplate.CHART.style.backgroundColor) : '' } } as IWidgetBackgroundStyle
}
