import { IWidgetBordersStyle, ITableWidgetColumnGroups, ITableWidgetColumnStyles, ITableWidgetConditionalStyle, ITableWidgetConditionalStyles, IWidgetCrossNavigation, ITableWidgetCustomMessages, IWidgetExports, ITableWidgetHeaders, IWidgetLinks, ITableWidgetPagination, IWidgetPreview, ITableWidgetRows, IWidgetRowsStyle, IWidgetSelection, IWidgetShadowsStyle, ITableWidgetSummaryRows, ITableWidgetSummaryStyle, ITableWidgetTooltipStyle, ITableWidgetVisibilityCondition, ITableWidgetVisualization, ITawbleWidgetHeadersStyle } from "../../../../Dashboard"
import descriptor from './TableWidgetHelpersDescriptor.json'
import deepcopy from 'deepcopy'

export const getDefaultConditionalStyles = () => {
    return deepcopy(descriptor.defaultConditionalStyles) as ITableWidgetConditionalStyles
}

export const getDefaultConditionalStyle = () => {
    return deepcopy(descriptor.defaultConditionalStyle) as ITableWidgetConditionalStyle
}

export const getDefaultColumnGroups = () => {
    return deepcopy(descriptor.defaultColumnGroups) as ITableWidgetColumnGroups
}

export const getDefaultExportsConfiguration = () => {
    return deepcopy(descriptor.defaultExportsConfiguration) as IWidgetExports
}

export const getDefaultHeadersConfiguration = () => {
    return deepcopy(descriptor.defaultHeadersConfiguration) as ITableWidgetHeaders
}

export const getDefaultRowsConfiguration = () => {
    return deepcopy(descriptor.defaultRowsConfiguration) as ITableWidgetRows
}

export const getDefaultSummaryRowsConfiguration = () => {
    return deepcopy(descriptor.defaultSummaryRowsConfiguration) as ITableWidgetSummaryRows
}

export const getDefaultCustomMessages = () => {
    return deepcopy(descriptor.defaultCustomMessages) as ITableWidgetCustomMessages
}

export const getDefaultSelection = () => {
    return deepcopy(descriptor.defaultSelection) as IWidgetSelection
}

export const getDefaultPagination = () => {
    return deepcopy(descriptor.defaultPagination) as ITableWidgetPagination
}

export const getDefaultColumnStyles = () => {
    return deepcopy(descriptor.defaultColumnStyles) as ITableWidgetColumnStyles
}

export const getDefaultHeadersStyle = () => {
    return deepcopy(descriptor.defaultHeadersStyle) as ITawbleWidgetHeadersStyle
}

export const getDefaultRowsStyle = () => {
    return deepcopy(descriptor.defaultRowsStyle) as IWidgetRowsStyle
}

export const getDefualtSummryStyle = () => {
    return deepcopy(descriptor.defaultSummaryStyle) as ITableWidgetSummaryStyle
}

export const getDefaultVisualizations = () => {
    return deepcopy(descriptor.defaultVisualizations) as ITableWidgetVisualization
}

export const getDefaultVisibilityCondition = () => {
    return deepcopy(descriptor.defaultVisibilityCondition) as ITableWidgetVisibilityCondition
}

export const getDefaultTooltips = () => {
    return deepcopy(descriptor.defaultTooltips) as ITableWidgetTooltipStyle[]
}