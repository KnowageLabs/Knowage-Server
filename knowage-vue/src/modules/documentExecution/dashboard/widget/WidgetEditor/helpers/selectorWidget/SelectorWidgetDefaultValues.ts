import { ISelectorWidgetDefaultValues, ISelectorWidgetSelectorType, ISelectorWidgetValuesManagement } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectorWidget'
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