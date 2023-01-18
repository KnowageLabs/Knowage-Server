import { ITableWidgetCustomMessages } from "../../Dashboard"
import { IDiscoveryWidgetConfiguration } from "../../interfaces/DashboardDiscoveryWidget"
import { getFormattedExport, getFormattedCustomMessages } from '../tableWidget/TableWidgetConfigurationHelper'

export const getFormattedDiscoveryConfiguration = (widget: any) => {
    return { exports: getFormattedExport(widget), customMessages: getFormattedCustomMessages(widget) as ITableWidgetCustomMessages } as IDiscoveryWidgetConfiguration
}
