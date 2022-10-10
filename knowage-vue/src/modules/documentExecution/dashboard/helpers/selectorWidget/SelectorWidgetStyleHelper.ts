import { IWidgetTitle } from "../../Dashboard"
import { ISelectorWidgetStyle } from "../../interfaces/DashboardSelectorWidget"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        label: {},
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
