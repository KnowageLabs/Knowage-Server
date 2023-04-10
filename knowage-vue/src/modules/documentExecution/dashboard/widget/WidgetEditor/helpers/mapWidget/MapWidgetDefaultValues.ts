import { IMapDialogSettings, IMapTooltipSettings, IMapWidgetLegend } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import descriptor from './MapWidgetDefaultValuesDescriptor.json'

export const getDefaultMapTooltips = () => {
    return descriptor.defaultTooltips as IMapTooltipSettings
}

export const getDefaultDialogSettings = () => {
    return descriptor.defaultDialogSettings as IMapDialogSettings
}

export const getDefaultLegendSettings = () => {
    return descriptor.defaultLegendSettings as IMapWidgetLegend
}