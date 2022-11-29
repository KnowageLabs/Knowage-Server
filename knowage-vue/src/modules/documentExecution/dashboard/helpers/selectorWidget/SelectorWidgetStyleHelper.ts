import { IWidgetBordersStyle, IWidgetPaddingStyle, IWidgetShadowsStyle } from "../../Dashboard"
import { ISelectorWidgetLabelStyle, ISelectorWidgetStyle } from "../../interfaces/DashboardSelectorWidget"
import { getFormattedBackgroundStyle, getFormattedTitleStyle } from "../common/WidgetStyleHelper"
import { hexToRgb } from '../FormattingHelpers'
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as selectorWidgetDefaultValues from '../../widget/WidgetEditor/helpers/selectorWidget/SelectorWidgetDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        label: getFormattedLabelStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        borders: getFormattedBorderStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        background: getFormattedBackgroundStyle(widget)
    } as ISelectorWidgetStyle
}


const getFormattedLabelStyle = (widget) => {
    if (!widget.style) return selectorWidgetDefaultValues.getDefaultLabelStyle()
    const formattedLabelStyle = {
        enabled: true,
        wrapText: widget.settings.wrapText ?? '',
        properties: {
            'font-weight': widget.style['font-weight'] ?? '',
            'font-style': widget.style['font-style'] ?? '',
            'font-size': widget.style['font-size'] ?? '',
            'font-family': widget.style['font-family'] ?? '',
            'justify-content': widget.style['justify-content'] ?? '',
            color: widget.style.color ?? '',
            'background-color': widget.style['background-color'] ?? '',
        }
    } as ISelectorWidgetLabelStyle

    return formattedLabelStyle
}

const getFormattedPaddingStyle = (widget: any) => {
    if (!widget.style || !widget.style.padding) return widgetCommonDefaultValues.getDefaultPaddingStyle()

    return {
        enabled: true,
        properties: {
            "padding-top": widget.style.padding['padding-top'],
            "padding-left": widget.style.padding['padding-left'],
            "padding-bottom": widget.style.padding['padding-bottom'],
            "padding-right": widget.style.padding['padding-right'],
            unlinked: widget.style.padding.unlinked
        }
    } as IWidgetPaddingStyle
}

const getFormattedBorderStyle = (widget: any) => {
    if (!widget.style || !widget.style.border) return widgetCommonDefaultValues.getDefaultBordersStyle()

    return { enabled: true, properties: { ...widget.style.border, 'border-color': widget.style.border['border-color'] } } as IWidgetBordersStyle
}

const getFormattedShadowsStyle = (widget: any) => {
    if (!widget.style || !widget.style.shadow) return widgetCommonDefaultValues.getDefaultShadowsStyle()

    return {
        enabled: true,
        properties: {
            "box-shadow": widget.style.shadow["box-shadow"],
            "color": hexToRgb(widget.style.backgroundColor)
        }
    } as IWidgetShadowsStyle
}
