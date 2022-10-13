import { IWidgetBackgroundStyle, IWidgetBordersStyle, IWidgetPaddingStyle, IWidgetShadowsStyle, IWidgetTitle } from '../../Dashboard'
import { hexToRgb } from '../FormattingHelpers'
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'

export const getFormattedTitleStyle = (widget: any) => {
    if (!widget.style || !widget.style.title) return widgetCommonDefaultValues.getDefaultTitleStyle()
    const formattedTitleStyle = {
        enabled: widget.style.titles,
        text: widget.style.title.label,
        height: widget.style.title.height,
        properties: { 'font-weight': '', 'font-style': '', 'font-size': '', 'font-family': '', 'justify-content': '', color: '', 'background-color': widget.style.title['background-color'] ?? '' }
    } as IWidgetTitle

    if (widget.style.title.font) {
        formattedTitleStyle.properties = {
            'font-weight': widget.style.title.font['font-weight'],
            'font-style': widget.style.title.font['font-style'],
            'font-size': widget.style.title.font['font-size'],
            'font-family': widget.style.title.font['font-family'],
            'justify-content': widget.style.title.font['text-align'],
            color: widget.style.title.font.color,
            'background-color': widget.style.title['background-color']
        }
    }

    return formattedTitleStyle
}

export const getFormattedPaddingStyle = (widget: any) => {
    if (!widget.style || !widget.style.padding) return widgetCommonDefaultValues.getDefaultPaddingStyle()

    return {
        enabled: widget.style.padding.enabled,
        properties: {
            "padding-top": widget.style.padding['padding-top'],
            "padding-left": widget.style.padding['padding-left'],
            "padding-bottom": widget.style.padding['padding-bottom'],
            "padding-right": widget.style.padding['padding-right'],
            unlinked: widget.style.padding.unlinked
        }
    } as IWidgetPaddingStyle
}

export const getFormattedBorderStyle = (widget: any) => {
    if (!widget.style || !widget.style.border) return widgetCommonDefaultValues.getDefaultBordersStyle()

    return { enabled: widget.style.borders, properties: { ...widget.style.border, 'border-color': hexToRgb(widget.style.border['border-color']) } } as IWidgetBordersStyle
}

export const getFormattedShadowsStyle = (widget: any) => {
    if (!widget.style || !widget.style.shadow) return widgetCommonDefaultValues.getDefaultShadowsStyle()

    return {
        enabled: widget.style.shadows,
        properties: {
            "box-shadow": widget.style.shadow["box-shadow"],
            "color": ''
        }
    } as IWidgetShadowsStyle
}

export const getFormattedBackgroundStyle = (widget: any) => {
    if (!widget.style || !widget.style.backgroundColor) return widgetCommonDefaultValues.getDefaultBackgroundStyle()
    return { enabled: true, "properties": { "background-color": widget.style.backgroundColor } } as IWidgetBackgroundStyle
}
