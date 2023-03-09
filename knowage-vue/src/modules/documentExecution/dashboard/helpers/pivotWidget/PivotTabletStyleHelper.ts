import { IPivotTableStyle } from '../../interfaces/pivotTable/DashboardPivotTableWidget'
import { getFormattedBorderStyle, getFormattedPaddingStyle, getFormattedShadowsStyle, getFormattedTitleStyle, getFormattedBackgroundStyle } from '../common/WidgetStyleHelper'
import * as pivotTableDefaultValues from '../../widget/WidgetEditor/helpers/pivotTableWidget/PivotTableDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        borders: getFormattedBorderStyle(widget),
        fields: getFormattedFieldsStyle(widget, 'measures'),
        fieldHeaders: getFormattedFieldsStyle(widget, 'measuresHeaders'),
        padding: getFormattedPaddingStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        background: getFormattedBackgroundStyle(widget),
        totals: getFormattedTotalsStyle(widget),
        subTotals: getFormattedSubTotalsStyle(widget),
        columnHeaders: getFormattedColumnHeadersStyle(widget),
        rowHeaders: pivotTableDefaultValues.getDefaultRowsHeadersStyle()
    } as IPivotTableStyle
}


const getFormattedFieldsStyle = (widget: any, fieldType) => {
    const formattedStyles = pivotTableDefaultValues.getDefaultFields()
    if (!widget.content.style || !widget.content.style[fieldType]) return formattedStyles
    else {
        formattedStyles.styles[0].properties = {
            'font-weight': widget.content.style[fieldType]['font-weight'],
            'font-style': widget.content.style[fieldType]['font-style'],
            'font-size': widget.content.style[fieldType]['font-size'],
            'font-family': widget.content.style[fieldType]['font-family'],
            'justify-content': widget.content.style[fieldType]['text-align'],
            color: widget.content.style[fieldType].color,
            'background-color': widget.content.style[fieldType]['background-color']
        }
        return formattedStyles
    }
}

const getFormattedTotalsStyle = (widget: any) => {
    const formattedStyles = pivotTableDefaultValues.getDefaultTotals()
    if (!widget.content.style || !widget.content.style.totals) return formattedStyles
    else {
        formattedStyles.enabled = true
        formattedStyles.properties['background-color'] = widget.content.style.totals['background-color']
        formattedStyles.properties.color = widget.content.style.totals.color

        return formattedStyles
    }
}
const getFormattedSubTotalsStyle = (widget: any) => {
    const formattedStyles = pivotTableDefaultValues.getDefaultTotals()
    if (!widget.content.style || !widget.content.style.subTotals) return formattedStyles
    else {
        formattedStyles.enabled = true
        formattedStyles.properties['background-color'] = widget.content.style.subTotals['background-color']
        formattedStyles.properties.color = widget.content.style.subTotals.color

        return formattedStyles
    }
}

const getFormattedColumnHeadersStyle = (widget: any) => {
    const defaultColumnHeadersStyle = pivotTableDefaultValues.getDefaultColumnHeadersStyle()
    if (!widget.content.style || !widget.content.style.crossTabHeaders) return defaultColumnHeadersStyle
    const oldCrossTabHeaders = widget.content.style.crossTabHeaders
    return {
        enabled: true,
        properties: {
            "background-color": oldCrossTabHeaders['background-color'] ?? defaultColumnHeadersStyle.properties['background-color'],
            color: oldCrossTabHeaders.color ?? defaultColumnHeadersStyle.properties.color,
            "font-family": oldCrossTabHeaders['font-family'] ?? defaultColumnHeadersStyle.properties['font-family'],
            "font-size": oldCrossTabHeaders['font-size'] ?? defaultColumnHeadersStyle.properties['font-size'],
            "font-style": oldCrossTabHeaders['font-style'] ?? defaultColumnHeadersStyle.properties['font-style'],
            "font-weight": oldCrossTabHeaders['font-weight'] ?? defaultColumnHeadersStyle.properties['font-weight'],
            "text-align": oldCrossTabHeaders['text-align'] ?? defaultColumnHeadersStyle.properties['text-align']
        }
    }
}
