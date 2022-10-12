import { IWidgetBackgroundStyle } from '@/modules/documentExecution/dashboard/Dashboard'
import descriptor from './SelectionsWidgetDefaultValuesDescriptor.json'
import deepcopy from 'deepcopy'
import { ISelectionsWidgetValuesManagement, ISelectionWidgetChipsStyle } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectionsWidget'


export const getDefaultBackgroundStyle = () => {
    return deepcopy(descriptor.defaultBackgroundStyle) as IWidgetBackgroundStyle
}

export const getDefaultValuesManagement = () => {
    return deepcopy(descriptor.defaultValuesManagement) as ISelectionsWidgetValuesManagement
}

export const getDefaultChipsStyle = () => {
    return deepcopy(descriptor.defaultChipsStyle) as ISelectionWidgetChipsStyle
}

