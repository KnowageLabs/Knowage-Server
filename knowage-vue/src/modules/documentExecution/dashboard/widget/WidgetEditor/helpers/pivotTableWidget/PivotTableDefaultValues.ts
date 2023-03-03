import { IPivotFieldPanel, IPivotFieldPicker, ITableWidgetColumnStyles } from '@/modules/documentExecution/dashboard/Dashboard'
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
    return deepcopy(descriptor.defaultTooltops)
}
