import { IDiscoveryWidgetSearchSettings } from "@/modules/documentExecution/dashboard/interfaces/DashboardDiscoveryWidget"
import deepcopy from "deepcopy"
import descriptor from './DiscoveryWidgetHelpersDescriptor.json'

export const getDefaultSearchSettings = () => {
    return deepcopy(descriptor.defaultSearchSettings) as IDiscoveryWidgetSearchSettings
}