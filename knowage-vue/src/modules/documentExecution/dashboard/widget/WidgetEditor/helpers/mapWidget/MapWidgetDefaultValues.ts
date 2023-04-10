import { IMapDialogSettings, IMapTooltipSettings } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import descriptor from './MapWidgetDefaultValuesDescriptor.json'

export const getDefaultMapTooltips = () => {
    return descriptor.defaultTooltips as IMapTooltipSettings
}

export const getDefaultDialogSettings = () => {
    return descriptor.defaultDialogSettings as IMapDialogSettings
}