import { ITableWidgetBordersStyle, ITableWidgetColumnGroups, ITableWidgetColumnStyle, ITableWidgetColumnStyles, ITableWidgetConditionalStyles, ITableWidgetCrossNavigation, ITableWidgetCustomMessages, ITableWidgetExports, ITableWidgetHeaders, ITableWidgetLinks, ITableWidgetPaddingStyle, ITableWidgetPagination, ITableWidgetPreview, ITableWidgetResponsive, ITableWidgetRows, ITableWidgetRowsStyle, ITableWidgetSelection, ITableWidgetShadowsStyle, ITableWidgetSummaryRows, ITableWidgetSummaryStyle, ITableWidgetTooltipStyle, ITableWidgetVisualization, ITawbleWidgetHeadersStyle } from "../../../../Dashboard"


export const getDefaultConditionalStyles = () => {
    return {
        enabled: false,
        conditions: []
    } as ITableWidgetConditionalStyles
}

export const getDefaultColumnGroups = () => {
    return {
        enabled: false,
        groups: []
    } as ITableWidgetColumnGroups
}

export const getDefaultExportsConfiguration = () => {
    return {
        pdf: {
            enabled: false,
            custom: {
                height: 0,
                width: 0,
                enabled: false
            },
            a4landscape: false,
            a4portrait: false
        },
        showExcelExport: false,
        showScreenshot: false
    } as ITableWidgetExports
}

export const getDefaultHeadersConfiguration = () => {
    return {
        enabled: false, enabledMultiline: false, custom: { enabled: false, rules: [] }
    } as ITableWidgetHeaders
}

export const getDefaultRowsConfiguration = () => {
    return {
        indexColumn: false,
        rowSpan: {
            enabled: false,
            column: ''
        }
    } as ITableWidgetRows
}

export const getDefaultSummaryRowsConfiguration = () => {
    return {
        enabled: false,
        list: [],
        style: { pinnedOnly: false }
    } as ITableWidgetSummaryRows
}

export const getDefaultCustomMessages = () => {
    return {
        hideNoRowsMessage: false,
        noRowsMessage: ''
    } as ITableWidgetCustomMessages
}

export const getDefaultCrossNavigation = () => {
    return {
        enabled: false,
        type: '',
        column: '',
        name: '',
        parameters: []
    } as ITableWidgetCrossNavigation
}

export const getDefaultLinks = () => {
    return {
        enabled: false,
        links: []
    } as ITableWidgetLinks
}

export const getDefaultPreview = () => {
    return {
        enabled: false,
        type: '',
        dataset: -1,
        parameters: [],
        directDownload: false
    } as ITableWidgetPreview
}


export const getDefaultSelection = () => {
    return {
        enabled: false,
        modalColumn: '',
        multiselection: {
            enabled: false,
            properties: {
                "background-color": '',
                color: ''
            }
        }
    } as ITableWidgetSelection
}

export const getDefaultPagination = () => {
    return { enabled: false, itemsNumber: 0 } as ITableWidgetPagination
}


export const getDefaultBordersStyle = () => {
    return {
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
}

export const getDefaultColumnStyles = () => {

    return {
        enabled: true,
        styles: [{
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
        }]
    } as ITableWidgetColumnStyles
}

export const getDefaultHeadersStyle = () => {
    return {
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
    } as ITawbleWidgetHeadersStyle
}

export const getDefaultPaddingStyle = () => {
    return {
        enabled: false,
        properties: {
            "padding-top": '',
            "padding-left": '',
            "padding-bottom": '',
            "padding-right": '',
            unlinked: false
        }
    } as ITableWidgetPaddingStyle
}

export const getDefaultRowsStyle = () => {
    return {
        height: 0,
        multiselectable: false,
        selectionColor: '',
        alternatedRows: {
            enabled: false,
            evenBackgroundColor: 'rgb(228, 232, 236)',
            oddBackgroundColor: ''

        }
    } as ITableWidgetRowsStyle
}

export const getDefaultShadowsStyle = () => {
    return {
        enabled: false,
        properties: {
            "box-shadow": '',
            "backgroundColor": ''
        }
    } as ITableWidgetShadowsStyle
}


export const getDefualtSummryStyle = () => {
    return {
        "background-color": "",
        "color": "",
        "font-family": "",
        "font-size": "",
        "font-style": "",
        "font-weight": "",
        "justify-content": ""
    } as ITableWidgetSummaryStyle
}

export const getDefaultVisualizations = () => {
    return {
        visualizationTypes: {
            enabled: false,
            types: [{
                target: 'all',
                type: 'Text',
                prefix: '',
                suffix: '',
                pinned: '',
            }]
        },
        visibilityConditions: {
            enabled: false,
            conditions: []
        }
    } as ITableWidgetVisualization
}

export const getDefaultTooltips = () => {
    const allTooltip = {
        target: 'all',
        enabled: false,
        prefix: '',
        suffix: '',
        precision: 0,
        header: {
            enabled: false,
            text: ''
        }
    }
    return [allTooltip] as ITableWidgetTooltipStyle[]
}

export const getDefaultResponsivnes = () => {
    return { xs: true, sm: true, md: true, lg: true, xl: true } as ITableWidgetResponsive
}