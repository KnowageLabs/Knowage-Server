import { IWidgetPaddingStyle, IWidgetResponsive, IWidgetShadowsStyle, IWidgetTitle } from "@/modules/documentExecution/dashboard/Dashboard"
import descriptor from './WidgetCommonDefaultValuesDescriptor.json'
import deepcopy from 'deepcopy'

export const getDefaultResponsivnes = () => {
    return deepcopy(descriptor.defaultResponsivnes) as IWidgetResponsive
}

export const getDefaultTitleStyle = () => {
    return deepcopy(descriptor.defaultTitleStyle) as IWidgetTitle
}

export const getDefaultPaddingStyle = () => {
    return deepcopy(descriptor.defaultPaddingStyle) as IWidgetPaddingStyle
}

export const getDefaultShadowsStyle = () => {
    return deepcopy(descriptor.defaultShadowsStyle) as IWidgetShadowsStyle
}