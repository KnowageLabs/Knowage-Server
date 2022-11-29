import { IWidgetRowsStyle } from '@/modules/documentExecution/dashboard/Dashboard'
import { ISelectionsWidgetNoSelections, ISelectionsWidgetValuesManagement, ISelectionWidgetChipsStyle } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectionsWidget'
import descriptor from './SelectionsWidgetDefaultValuesDescriptor.json'
import deepcopy from 'deepcopy'

export const getDefaultValuesManagement = () => {
    return deepcopy(descriptor.defaultValuesManagement) as ISelectionsWidgetValuesManagement
}

export const getDefaultNoSelectionsConfiguration = () => {
    return deepcopy(descriptor.defaultNoSelectionsConfiguration) as ISelectionsWidgetNoSelections
}

export const getDefaultChipsStyle = () => {
    return deepcopy(descriptor.defaultChipsStyle) as ISelectionWidgetChipsStyle
}

export const getDefaultRowsStyle = () => {
    return deepcopy(descriptor.defaultRowsStyle) as IWidgetRowsStyle
}
