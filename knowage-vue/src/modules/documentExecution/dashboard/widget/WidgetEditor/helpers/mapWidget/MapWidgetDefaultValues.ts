import { IMapTooltipSettings } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import descriptor from './MapWidgetDefaultValuesDescriptor.json'

export const getDefaultMapTooltips = () => {
    return descriptor.defaultTooltips as IMapTooltipSettings
}