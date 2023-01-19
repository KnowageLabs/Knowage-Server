import { IDashboard, ITableWidgetStyle, IWidget, IDashboardDriver, IWidgetInteractions, IWidgetResponsive } from "../../Dashboard"
import { IDiscoveryWidgetSettings, IDiscoveryWidgetConfiguration, IDiscoveryWidgetFacetsSettings, IDiscoveryWidgetSearchSettings, IDiscoveryWidgetFacetsColumnSettings } from "../../interfaces/DashboardDiscoveryWidget"
import { getFormattedWidgetColumns } from "../common/WidgetColumnHelper"
import { getFormattedInteractions } from "../common/WidgetInteractionsHelper"
import { getFiltersForColumns } from "../DashboardBackwardCompatibilityHelper"
import { getFormattedStyle } from "../tableWidget/TableWidgetStyleHelper"
import { getFormattedDiscoveryConfiguration } from "./DiscoveryWidgetConfigurationHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as  discoveryWidgetDefaultValues from '../../widget/WidgetEditor/helpers/discoveryWidget/DiscoveryWidgetDefaultValues'


const columnNameIdMap = {}

export const formatDiscoveryWidget = (widget: any, drivers: IDashboardDriver[]) => {

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
    getSettingsFromWidgetColumns(formattedWidget, widget)

    console.log(">>>>>>>>>>> FORMATTED WIDGET MODEL: ", formattedWidget)

    return formattedWidget
}


const getFormattedWidgetSettings = (widget: any, drivers: IDashboardDriver[]) => {
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
    const formattedFacetSettings = { enabled: true, columns: [{ ...discoveryWidgetDefaultValues.getDefaultFacetsColumnSettings(), names: 'all' }] } as IDiscoveryWidgetFacetsSettings
    if (!widget.settings.facets) return formattedFacetSettings
    formattedFacetSettings.enabled = widget.settings.facets.enabled
    formattedFacetSettings.columns[0].limit = widget.settings.facets.limit
    formattedFacetSettings.columns[0].precision = widget.settings.facets.precision
    formattedFacetSettings.columns[0].selection = widget.settings.facets.selection
    return formattedFacetSettings
}

const getFormattedSearchSettings = (widget: any, drivers: IDashboardDriver[]) => {
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

const formattSearchSettingsWithDriverValue = (driverLabel: string | undefined, drivers: IDashboardDriver[], formattedSearchSettings: IDiscoveryWidgetSearchSettings) => {
    const index = drivers.findIndex((driver: IDashboardDriver) => driver.driverLabel === driverLabel)
    if (index !== -1) {
        formattedSearchSettings.driverId = drivers[index].urlName
        formattedSearchSettings.defaultValue = drivers[index].value ? drivers[index].value : ''
    }
}

const getSettingsFromWidgetColumns = (formattedWidget: IWidget, widget: any) => {
    const allColumnsFacetsSettings = formattedWidget.settings.facets.columns[0] as IDiscoveryWidgetFacetsColumnSettings
    for (let i = 0; i < widget.content.columnSelectedOfDataset.length; i++) {
        const tempColumn = widget.content.columnSelectedOfDataset[i]
        if (tempColumn.facet) getFacetsSettingsFromWidgetColumn(formattedWidget, tempColumn, allColumnsFacetsSettings)
    }
}

const getFacetsSettingsFromWidgetColumn = (formattedWidget: IWidget, tempColumn: any, allColumnsFacetsSettings: IDiscoveryWidgetFacetsColumnSettings) => {
    const formattedFacetSettings = {
        names: [getColumnId(tempColumn.name)],
        selection: allColumnsFacetsSettings.selection,
        closedByDefault: allColumnsFacetsSettings.closedByDefault,
        width: allColumnsFacetsSettings.width,
        limit: allColumnsFacetsSettings.limit,
        precision: allColumnsFacetsSettings.precision,
        aggregationSelected: tempColumn.aggregationSelected,
        aggregationColumn: tempColumn.aggregationColumn
    }
    formattedWidget.settings.facets.columns.push(formattedFacetSettings)
}



const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}
