import { ISelectionWidgetChipsStyle, ISelectionWidgetStyle } from '../../interfaces/DashboardSelectionsWidget'
import { getFormattedTitleStyle, getFormattedPaddingStyle, getFormattedBorderStyle, getFormattedShadowsStyle, getFormattedBackgroundStyle } from '../common/WidgetStyleHelper'
import { IWidgetRowsStyle } from '../../Dashboard'
import * as selectionsWidgetDefaultValues from '../../widget/WidgetEditor/helpers/selectionsWidget/SelectionsWidgetDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        chips: getFormattedChipsStyle(widget),
        rows: getFormattedRowsStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        borders: getFormattedBorderStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        background: getFormattedBackgroundStyle(widget)
    } as ISelectionWidgetStyle
}

const getFormattedChipsStyle = (widget: any) => {
    if (!widget.style || !widget.style.chips) return selectionsWidgetDefaultValues.getDefaultChipsStyle()
    return {
        height: 0,
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

const getFormattedRowsStyle = (widget: any) => {
    const formattedRowsStyle = {
        height: widget.style.row?.height ?? 0,

        alternatedRows: {
            enabled: widget.style.alternateRows?.enabled ?? false,
            evenBackgroundColor: widget.style.alternateRows?.evenRowsColor ?? 'rgb(228, 232, 236)',
            oddBackgroundColor: widget.style.alternateRows?.oddRowsColor ?? ''
        }
    } as IWidgetRowsStyle
    return formattedRowsStyle
}
