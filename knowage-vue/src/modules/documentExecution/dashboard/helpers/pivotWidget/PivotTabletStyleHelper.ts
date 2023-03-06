import { IPivotTableStyle } from '../../interfaces/pivotTable/DashboardPivotTableWidget'
import { getFormattedBorderStyle, getFormattedPaddingStyle, getFormattedShadowsStyle, getFormattedTitleStyle, getFormattedBackgroundStyle } from '../common/WidgetStyleHelper'
import * as pivotTalbeDefaultValues from '../../widget/WidgetEditor/helpers/pivotTableWidget/PivotTableDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        borders: getFormattedBorderStyle(widget),
        columns: pivotTalbeDefaultValues.getDefaultColumnStyles(),
        headers: getFormattedHeadersStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        rows: getFormattedRowsStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        background: getFormattedBackgroundStyle(widget),
        totals: getFormattedTotalsStyle(widget),
        subTotals: getFormattedSubTotalsStyle(widget)
    } as IPivotTableStyle
}

const getFormattedHeadersStyle = (widget: any) => {}
const getFormattedRowsStyle = (widget: any) => {}

const getFormattedTotalsStyle = (widget: any) => {
    const formattedStyles = pivotTalbeDefaultValues.getDefaultTotals()
    if (!widget.content.style || !widget.content.style.totals) return formattedStyles
    else {
        formattedStyles.enabled = true
        formattedStyles.properties['background-color'] = widget.content.style.totals['background-color']
        formattedStyles.properties.color = widget.content.style.totals.color

        return formattedStyles
    }
}
const getFormattedSubTotalsStyle = (widget: any) => {
    const formattedStyles = pivotTalbeDefaultValues.getDefaultTotals()
    if (!widget.content.style || !widget.content.style.subTotals) return formattedStyles
    else {
        formattedStyles.enabled = true
        formattedStyles.properties['background-color'] = widget.content.style.subTotals['background-color']
        formattedStyles.properties.color = widget.content.style.subTotals.color

        return formattedStyles
    }
}
