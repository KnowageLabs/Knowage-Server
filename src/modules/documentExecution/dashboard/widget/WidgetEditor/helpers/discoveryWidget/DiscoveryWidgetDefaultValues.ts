import { IDiscoveryWidgetFacetsSettings, IDiscoveryWidgetSearchSettings } from "@/modules/documentExecution/dashboard/interfaces/DashboardDiscoveryWidget"
import deepcopy from "deepcopy"
import descriptor from './DiscoveryWidgetDefaultValuesDescriptor.json'

export const getDefaultFacetsSettings = () => {
    return deepcopy(descriptor.defaultFacetsSettings) as IDiscoveryWidgetFacetsSettings
}

export const getDefaultSearchSettings = () => {
    return deepcopy(descriptor.defaultSearchSettings) as IDiscoveryWidgetSearchSettings
}