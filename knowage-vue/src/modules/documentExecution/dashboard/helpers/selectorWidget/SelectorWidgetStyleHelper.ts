import { IWidgetTitle } from "../../Dashboard"
import { ISelectorWidgetLabelStyle, ISelectorWidgetStyle } from "../../interfaces/DashboardSelectorWidget"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as selectorWidgetDefaultValues from '../../widget/WidgetEditor/helpers/selectorWidget/SelectorWidgetDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        label: getFormattedLabelStyle(widget),
        background: {},
        padding: {},
        borders: {}
    } as ISelectorWidgetStyle
}

const getFormattedTitleStyle = (widget: any) => {
    if (!widget.style || !widget.style.title) return widgetCommonDefaultValues.getDefaultTitleStyle()
    const formattedTitleStyle = {
        enabled: widget.style.titles,
        text: widget.style.title.label,
        height: widget.style.title.height,
        properties: {
            'font-weight': widget.style.title.font['font-weight'] ?? '',
            'font-style': widget.style.title.font['font-style'] ?? '',
            'font-size': widget.style.title.font['font-size'] ?? '',
            'font-family': widget.style.title.font['font-family'] ?? '',
            'justify-content': widget.style.title.font['text-align'] ?? '',
            color: widget.style.title.font.color ?? '',
            'background-color': widget.style.title['background-color'] ?? ''
        }
    } as IWidgetTitle

    return formattedTitleStyle

}

const getFormattedLabelStyle = (widget) => {
    if (!widget.style) return selectorWidgetDefaultValues.getDefaultLabelStyle()
    const formattedLabelStyle = {
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