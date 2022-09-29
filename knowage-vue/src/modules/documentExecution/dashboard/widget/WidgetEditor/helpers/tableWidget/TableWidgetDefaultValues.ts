import { ITableWidgetBordersStyle, ITableWidgetColumnGroups, ITableWidgetColumnStyles, ITableWidgetConditionalStyle, ITableWidgetConditionalStyles, ITableWidgetCrossNavigation, ITableWidgetCustomMessages, ITableWidgetExports, ITableWidgetHeaders, ITableWidgetLinks, ITableWidgetPaddingStyle, ITableWidgetPagination, ITableWidgetPreview, ITableWidgetResponsive, ITableWidgetRows, ITableWidgetRowsStyle, ITableWidgetSelection, ITableWidgetShadowsStyle, ITableWidgetSummaryRows, ITableWidgetSummaryStyle, ITableWidgetTooltipStyle, ITableWidgetVisibilityCondition, ITableWidgetVisualization, ITawbleWidgetHeadersStyle } from "../../../../Dashboard"
import descriptor from './TableWidgetHelpersDescriptor.json'

export const getDefaultConditionalStyles = () => {
    return descriptor.defaultConditionalStyles as ITableWidgetConditionalStyles
}

export const getDefaultConditionalStyle = () => {
    return descriptor.defaultConditionalStyle as ITableWidgetConditionalStyle
}

export const getDefaultColumnGroups = () => {
    return descriptor.defaultColumnGroups as ITableWidgetColumnGroups
}

export const getDefaultExportsConfiguration = () => {
    return descriptor.defaultExportsConfiguration as ITableWidgetExports
}

export const getDefaultHeadersConfiguration = () => {
    return descriptor.defaultHeadersConfiguration as ITableWidgetHeaders
}

export const getDefaultRowsConfiguration = () => {
    return descriptor.defaultRowsConfiguration as ITableWidgetRows
}

export const getDefaultSummaryRowsConfiguration = () => {
    return descriptor.defaultSummaryRowsConfiguration as ITableWidgetSummaryRows
}

export const getDefaultCustomMessages = () => {
    return descriptor.defaultCustomMessages as ITableWidgetCustomMessages
}

export const getDefaultCrossNavigation = () => {
    return descriptor.defaultCrossNavigation as ITableWidgetCrossNavigation
}

export const getDefaultLinks = () => {
    return descriptor.defaultLinks as ITableWidgetLinks
}

export const getDefaultPreview = () => {
    return descriptor.defaultPreview as ITableWidgetPreview
}


export const getDefaultSelection = () => {
    return descriptor.defaultSelection as ITableWidgetSelection
}

export const getDefaultPagination = () => {
    return descriptor.defaultPagination as ITableWidgetPagination
}


export const getDefaultBordersStyle = () => {
    return descriptor.defaultBordersStyle as ITableWidgetBordersStyle
}

export const getDefaultColumnStyles = () => {
    return descriptor.defaultColumnStyles as ITableWidgetColumnStyles
}

export const getDefaultHeadersStyle = () => {
    return descriptor.defaultHeadersStyle as ITawbleWidgetHeadersStyle
}

export const getDefaultPaddingStyle = () => {
    return descriptor.defaultPaddingStyle as ITableWidgetPaddingStyle
}

export const getDefaultRowsStyle = () => {
    return descriptor.defaultRowsStyle as ITableWidgetRowsStyle
}

export const getDefaultShadowsStyle = () => {
    return descriptor.defaultShadowsStyle as ITableWidgetShadowsStyle
}


export const getDefualtSummryStyle = () => {
    return descriptor.defaultSummaryStyle as ITableWidgetSummaryStyle
}

export const getDefaultVisualizations = () => {
    return descriptor.defaultVisualizations as ITableWidgetVisualization
}

export const getDefaultVisibilityCondition = () => {
    return descriptor.defaultVisibilityCondition as ITableWidgetVisibilityCondition
}

export const getDefaultTooltips = () => {
    return descriptor.defaultTooltips as ITableWidgetTooltipStyle[]
}

export const getDefaultResponsivnes = () => {
    return descriptor.defaultResponsivnes as ITableWidgetResponsive
}