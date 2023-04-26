import { IWidgetRowsStyle, ITableWidgetStyle } from "../../Dashboard"
import { convertColorFromHSLtoRGB } from '../FormattingHelpers'
import { getFormattedPaddingStyle, getFormattedBorderStyle, getFormattedShadowsStyle, getFormattedBackgroundStyle, getFormattedTitleStyle } from '../common/WidgetStyleHelper'
import * as  tableWidgetDefaultValues from '../../widget/WidgetEditor/helpers/tableWidget/TableWidgetDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        borders: getFormattedBorderStyle(widget),
        columns: tableWidgetDefaultValues.getDefaultColumnStyles(),
        columnGroups: getDefaultColumnGroupsStyle(widget),
        headers: getFormattedHeadersStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        rows: getFormattedRowsStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        summary: getFormattedSummaryStyle(widget),
        background: getFormattedBackgroundStyle(widget)
    } as ITableWidgetStyle
}


const getDefaultColumnGroupsStyle = (widget: any) => {
    const formattedColumnGroupsStyles = tableWidgetDefaultValues.getDefaultColumnStyles()
    if (!widget.groups) return formattedColumnGroupsStyles
    let fields = ['background-color', 'color', "justify-content", "font-size", "font-family", "font-style", "font-weight"]
    for (let i = 0; i < widget.groups.length; i++) {
        const tempGroup = widget.groups[i]
        let hasStyle = false;
        for (let j = 0; j < fields.length; j++) {
            if (tempGroup.hasOwnProperty(fields[j])) {
                hasStyle = true;
                break
            }
        }
        if (hasStyle) formattedColumnGroupsStyles.styles.push({
            target: [tempGroup.id], properties: {
                width: 0,
                "background-color": tempGroup['background-color'] ?? "rgb(0, 0, 0)",
                color: tempGroup.color ?? 'rgb(255, 255, 255)',
                "justify-content": tempGroup['justify-content'] ?? '',
                "font-size": tempGroup['font-size'] ?? "",
                "font-family": tempGroup['font-family'] ?? '',
                "font-style": tempGroup['font-style'] ?? '',
                "font-weight": tempGroup['font-weight'] ?? '',
            }
        })
    }
    return formattedColumnGroupsStyles
}

const getFormattedHeadersStyle = (widget: any) => {
    if (!widget.style?.th) return tableWidgetDefaultValues.getDefaultHeadersStyle()

    return {
        height: widget.style.th.height ?? 25,
        properties: {
            "background-color": widget.style.th['background-color'] ?? "rgb(137, 158, 175)",
            color: widget.style.th.color ?? 'rgb(255, 255, 255)',
            "justify-content": widget.style.th['justify-content'] ?? 'center',
            "font-size": widget.style.th['font-size'] ?? "14px",
            "font-family": widget.style.th['font-family'] ?? '',
            "font-style": widget.style.th['font-style'] ?? 'normal',
            "font-weight": widget.style.th['font-weight'] ?? '',
        }
    }
}

const getFormattedRowsStyle = (widget: any) => {
    const formattedRowsStyle = {
        height: getPixelValueAsNumber(widget.style.tr?.height) ?? 25,
        multiselectable: widget.settings.multiselectable ?? false,
        selectionColor: widget.settings.multiselectablecolor ?? '',
        alternatedRows: {
            enabled: widget.settings.alternateRows?.enabled ?? false,
            evenBackgroundColor: widget.settings.alternateRows?.evenRowsColor ?? 'rgb(228, 232, 236)',
            oddBackgroundColor: widget.settings.alternateRows?.oddRowsColor ?? ''

        }
    }
    return formattedRowsStyle as IWidgetRowsStyle
}

const getPixelValueAsNumber = (pixelValue: string | number | undefined) => {
    if (!pixelValue || typeof pixelValue === 'number') return pixelValue
    else if (typeof pixelValue === 'string') return +pixelValue.substring(0, pixelValue.indexOf('px'))
}

const getFormattedSummaryStyle = (widget: any) => {
    if (!widget.settings.summary || !widget.settings.summary.style) return tableWidgetDefaultValues.getDefualtSummryStyle()

    return {
        "background-color": convertColorFromHSLtoRGB(widget.settings.summary.style['background-color']),
        "color": convertColorFromHSLtoRGB(widget.settings.summary.style.color),
        "font-family": widget.settings.summary.style['font-family'] ?? '',
        "font-size": widget.settings.summary.style['font-size'] ?? '',
        "font-style": widget.settings.summary.style['font-style'] ?? '',
        "font-weight": widget.settings.summary.style['font-weight'] ?? '',
        "justify-content": ""
    }
}
