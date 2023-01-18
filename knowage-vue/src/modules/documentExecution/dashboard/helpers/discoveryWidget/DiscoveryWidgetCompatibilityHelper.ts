import { IDashboard, ITableWidgetStyle, IWidget, IDashboardDatasetDriver, IWidgetInteractions, IWidgetResponsive } from "../../Dashboard"
import { IDiscoveryWidgetSettings, IDiscoveryWidgetConfiguration, IDiscoveryWidgetFacetsSettings, IDiscoveryWidgetSearchSettings } from "../../interfaces/DashboardDiscoveryWidget"
import { getFormattedWidgetColumns } from "../common/WidgetColumnHelper"
import { getFormattedInteractions } from "../common/WidgetInteractionsHelper"
import { getFiltersForColumns } from "../DashboardBackwardCompatibilityHelper"
import { getFormattedStyle } from "../tableWidget/TableWidgetStyleHelper"
import { getFormattedDiscoveryConfiguration } from "./DiscoveryWidgetConfigurationHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as  discoveryWidgetDefaultValues from '../../widget/WidgetEditor/helpers/discoveryWidget/DiscoveryWidgetDefaultValues'


const columnNameIdMap = {}

export const formatDiscoveryWidget = (widget: any, drivers: IDashboardDatasetDriver[]) => {

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
    formattedWidget.settings = getFormattedWidgetSettings(widget, drivers)
    getFiltersForColumns(formattedWidget, widget)
    // getSettingsFromWidgetColumns(formattedWidget, widget, formattedDashboardModel)

    console.log(">>>>>>>>>>> FORMATTED WIDGET MODEL: ", formattedWidget)

    return formattedWidget
}


const getFormattedWidgetSettings = (widget: any, drivers: IDashboardDatasetDriver[]) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        facets: getFormattedFacetsSettings(widget) as IDiscoveryWidgetFacetsSettings,
        search: getFormattedSearchSettings(widget, drivers) as IDiscoveryWidgetSearchSettings,
        configuration: getFormattedDiscoveryConfiguration(widget) as IDiscoveryWidgetConfiguration,
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        style: getFormattedStyle(widget) as ITableWidgetStyle,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes() as IWidgetResponsive
    } as IDiscoveryWidgetSettings
    return formattedSettings
}

const getFormattedFacetsSettings = (widget: any) => {
    return {}
}

const getFormattedSearchSettings = (widget: any, drivers: IDashboardDatasetDriver[]) => {
    console.log(">>>>>>>> drivers: ", drivers)
    const formattedSearchSettings = discoveryWidgetDefaultValues.getDefaultSearchSettings()
    if (!widget.search) return formattedSearchSettings
    formattedSearchSettings.columns = widget.search.columns
    formattedSearchSettings.default = widget.settings.defaultTextSearch
    formattedSearchSettings.defaultType = widget.settings.defaultTextSearchType

    if (widget.settings.defaultTextSearchType === 'static')
        formattedSearchSettings.defaultValue = widget.search.text
    else formattSearchSettingsWithDriverValue(widget.settings.defaultTextSearchValue, drivers, formattedSearchSettings)

    return formattedSearchSettings
}

const formattSearchSettingsWithDriverValue = (driverLabel: string | undefined, drivers: IDashboardDatasetDriver[], formattedSearchSettings: IDiscoveryWidgetSearchSettings) => {
    const index = drivers.findIndex((driver: IDashboardDatasetDriver) => driver.driverLabel === driverLabel)
    if (index !== -1) {
        formattedSearchSettings.driverId = drivers[index].urlName
        formattedSearchSettings.defaultValue = drivers[index].parameterValue[0] ? "" + drivers[index].parameterValue[0].value : ''
    }
}

const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}
