import { ISelectorWidgetDefaultValues, ISelectorWidgetLabelStyle, ISelectorWidgetSelectorType, ISelectorWidgetValuesManagement } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectorWidget'
import { IWidgetBackgroundStyle } from '@/modules/documentExecution/dashboard/Dashboard'
import descriptor from './SelectorWidgetDefaultValuesDescriptor.json'
import deepcopy from 'deepcopy'

export const getDefaultSelectorType = () => {
    return deepcopy(descriptor.defaultSelectorType) as ISelectorWidgetSelectorType
}

export const getDefaultValues = () => {
    return deepcopy(descriptor.defaultValues) as ISelectorWidgetDefaultValues
}

export const getDefaultValuesManagement = () => {
    return deepcopy(descriptor.defaultValuesManagement) as ISelectorWidgetValuesManagement
}

export const getDefaultLabelStyle = () => {
    return deepcopy(descriptor.defaultLabelStyle) as ISelectorWidgetLabelStyle
}

export const getDefaultBackgroundStyle = () => {
    return deepcopy(descriptor.defaultBackgroundStyle) as IWidgetBackgroundStyle
}
