import { IDashboard, ITableWidgetStyle, IWidget, IWidgetInteractions, IWidgetResponsive } from "../../Dashboard"
import { IDiscoveryWidgetSettings, IDiscoveryWidgetConfiguration, IDiscoveryWidgetFacetsSettings, IDiscoveryWidgetSearchSettings } from "../../interfaces/DashboardDiscoveryWidget"
import { getFormattedWidgetColumns } from "../common/WidgetColumnHelper"
import { getFormattedInteractions } from "../common/WidgetInteractionsHelper"
import { getFiltersForColumns } from "../DashboardBackwardCompatibilityHelper"
import { getFormattedStyle } from "../tableWidget/TableWidgetStyleHelper"
import { getFormattedDiscoveryConfiguration } from "./DiscoveryWidgetConfigurationHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'


const columnNameIdMap = {}

export const formatDiscoveryWidget = (widget: any, formattedDashboardModel: IDashboard) => {

    console.log(">>>>>>>>>>> OLD WIDGET MODEL: ", widget)

    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId,
        type: widget.type,
        columns: getFormattedWidgetColumns(widget, columnNameIdMap),
        theme: '',
        style: {},
        settings: {} as IDiscoveryWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget, formattedDashboardModel)
    getFiltersForColumns(formattedWidget, widget)
    // getSettingsFromWidgetColumns(formattedWidget, widget, formattedDashboardModel)

    console.log(">>>>>>>>>>> FORMATTED WIDGET MODEL: ", formattedWidget)

    return formattedWidget
}


const getFormattedWidgetSettings = (widget: any, formattedDashboardModel: IDashboard) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        facets: {} as IDiscoveryWidgetFacetsSettings,
        search: {} as IDiscoveryWidgetSearchSettings,
        configuration: getFormattedDiscoveryConfiguration(widget) as IDiscoveryWidgetConfiguration,
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        style: getFormattedStyle(widget) as ITableWidgetStyle,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes() as IWidgetResponsive
    } as IDiscoveryWidgetSettings
    return formattedSettings
}

const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}
