import { IVegaChartsStyle } from './../../../interfaces/vega/VegaChartsWidget.d';
import { getFormattedTitleStyle, getFormattedPaddingStyle, getFormattedBorderStyle, getFormattedShadowsStyle, getFormattedBackgroundStyle } from '../../common/WidgetStyleHelper'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        borders: getFormattedBorderStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        background: getFormattedBackgroundStyle(widget)
    } as IVegaChartsStyle
}
