import { IPivotFieldPanel, IPivotFieldPicker, IPivotTooltips, IPivotTotal, ITableWidgetColumnStyles } from '@/modules/documentExecution/dashboard/Dashboard'
import { IPivotTableColumnHeadersStyle } from '@/modules/documentExecution/dashboard/interfaces/pivotTable/DashboardPivotTableWidget'
import deepcopy from 'deepcopy'
import descriptor from './PivotTableDefaultValuesDescriptor.json'

export const getDefaultColumnStyles = () => {
    return deepcopy(descriptor.defaultColumnStyles) as ITableWidgetColumnStyles
}

export const getDefaultFieldPicker = () => {
    return deepcopy(descriptor.defaultFieldPicker) as IPivotFieldPicker
}

export const getDefaultFieldPanel = () => {
    return deepcopy(descriptor.defaultFieldPanel) as IPivotFieldPanel
}

export const getDefaultTooltips = () => {
    return deepcopy(descriptor.defaultTooltips) as IPivotTooltips[]
}

export const getDefaultTotals = () => {
    return deepcopy(descriptor.defaultTotals) as IPivotTotal
}

export const getDefaultFields = () => {
    return deepcopy(descriptor.defaultColumnStyles) as ITableWidgetColumnStyles
}

export const getDefaultColumnHeadersStyle = () => {
    return deepcopy(descriptor.defaultColumnHeadersStyle) as IPivotTableColumnHeadersStyle
}
