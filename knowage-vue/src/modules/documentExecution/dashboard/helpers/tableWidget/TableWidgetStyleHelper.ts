import { ITableWidgetBordersStyle, ITableWidgetColumnStyle, ITableWidgetPaddingStyle, ITableWidgetRowsStyle, ITableWidgetShadowsStyle, ITableWidgetStyle } from "../../Dashboard"
import { hexToRgb, convertColorFromHSLtoRGB } from '../FormattingHelpers'


export const getFormattedStyle = (widget: any) => {
    return {
        borders: getFormattedBorderStyle(widget),
        columns: [{
            target: 'all',
            properties: {
                "background-color": '',
                color: '',
                "justify-content": '',
                "font-size": '',
                "font-family": '',
                "font-style": '',
                "font-weight": ''
            }
        }],
        columnGroups: getFormattedColumnGroupsStyle(widget),
        headers: getFormattedHeadersStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        rows: getFormattedRowsStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        summary: getFormattedSummaryStyle(widget)
    } as ITableWidgetStyle
}

const getFormattedBorderStyle = (widget: any) => {
    if (!widget.style || !widget.style.border) return {
        enabled: false,
        properties: {
            "border-bottom-left-radius": "",
            "border-bottom-right-radius": "",
            "border-style": "",
            "border-top-left-radius": "",
            "border-top-right-radius": "",
            "border-width": "",
            "border-color": "rgb(212, 212, 212)"
        }
    } as ITableWidgetBordersStyle

    return { enabled: true, properties: { ...widget.style.border, 'border-color': hexToRgb(widget.style.border['border-color']) } } as ITableWidgetBordersStyle
}

const getFormattedColumnGroupsStyle = (widget: any) => {
    const formattedColumnGroupsStyles = [{
        target: 'all',
        properties: {
            "background-color": '',
            color: '',
            "justify-content": '',
            "font-size": '',
            "font-family": '',
            "font-style": '',
            "font-weight": ''
        }
    }] as ITableWidgetColumnStyle[]
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
        if (hasStyle) formattedColumnGroupsStyles.push({
            target: [tempGroup.id], properties: {
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
    if (!widget.style?.th) return {
        height: 25,
        properties: {
            "background-color": "rgb(137, 158, 175)",
            color: 'rgb(255, 255, 255)',
            "justify-content": 'center',
            "font-size": "14px",
            "font-family": "",
            "font-style": "normal",
            "font-weight": "",
        }
    }


    return {
        height: widget.style.th.height,
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

const getFormattedPaddingStyle = (widget: any) => {
    if (!widget.style || !widget.style.padding) return {
        enabled: false,
        properties: {
            "padding-top": '',
            "padding-left": '',
            "padding-bottom": '',
            "padding-right": '',
            unlinked: false
        }
    } as ITableWidgetPaddingStyle

    return {
        enabled: widget.style.padding.enabled,
        properties: {
            "padding-top": widget.style.padding['padding-top'],
            "padding-left": widget.style.padding['padding-left'],
            "padding-bottom": widget.style.padding['padding-bottom'],
            "padding-right": widget.style.padding['padding-right'],
            unlinked: widget.style.padding.unlinked
        }
    } as ITableWidgetPaddingStyle
}

const getFormattedRowsStyle = (widget: any) => {
    const formattedRowsStyle = {
        height: widget.style.tr?.height ?? 0,
        multiselectable: widget.settings.multiselectable ?? false,
        selectionColor: widget.settings.multiselectablecolor ?? '',
        alternatedRows: {
            enabled: widget.settings.alternateRows.enabled ?? false,
            evenBackgroundColor: widget.settings.alternateRows.evenRowsColor ?? 'rgb(228, 232, 236)',
            oddBackgroundColor: widget.settings.alternateRows.oddRowsColor ?? ''

        }
    }
    return formattedRowsStyle as ITableWidgetRowsStyle
}

const getFormattedShadowsStyle = (widget: any) => {
    if (!widget.style || !widget.style.shadow) return {
        enabled: false,
        properties: {
            "box-shadow": '',
            "backgroundColor": ''
        }
    } as ITableWidgetShadowsStyle

    return {
        enabled: true,
        properties: {
            "box-shadow": widget.style.shadow["box-shadow"],
            "backgroundColor": hexToRgb(widget.style.backgroundColor)
        }
    } as ITableWidgetShadowsStyle
}


const getFormattedSummaryStyle = (widget: any) => {
    if (!widget.settings.summary || !widget.settings.summary.style) return {
        "background-color": "",
        "color": "",
        "font-family": "",
        "font-size": "",
        "font-style": "",
        "font-weight": "",
        "justify-content": ""
    }

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

