import { IMapWidgetStyle } from './../../interfaces/mapWidget/DashboardMapWidget.d';
import { getFormattedTitleStyle, getFormattedBorderStyle, getFormattedBackgroundStyle, getFormattedPaddingStyle, getFormattedShadowsStyle } from './../common/WidgetStyleHelper';

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        borders: getFormattedBorderStyle(widget),
        background: getFormattedBackgroundStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
    } as IMapWidgetStyle
}
