import { ISelectionWidgetChipsStyle, ISelectionWidgetStyle } from "../../interfaces/DashboardSelectionsWidget"
import { getFormattedTitleStyle, getFormattedPaddingStyle, getFormattedBorderStyle, getFormattedShadowsStyle } from '../common/WidgetStyleHelper'
import * as selectionsWidgetDefaultValues from '../../widget/WidgetEditor/helpers/selectionsWidget/SelectionsWidgetDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        chips: getFormattedChipsStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        borders: getFormattedBorderStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        background: selectionsWidgetDefaultValues.getDefaultBackgroundStyle()
    } as ISelectionWidgetStyle
}


const getFormattedChipsStyle = (widget: any) => {
    if (!widget.style || !widget.style.chips) return selectionsWidgetDefaultValues.getDefaultChipsStyle()
    return {
        height: '',
        properties: {
            'font-weight': '',
            'font-style': '',
            'font-size': '',
            'font-family': '',
            'justify-content': '',
            color: widget.style.chips.color,
            'background-color': widget.style.chips['background-color']
        }
    } as ISelectionWidgetChipsStyle
}

