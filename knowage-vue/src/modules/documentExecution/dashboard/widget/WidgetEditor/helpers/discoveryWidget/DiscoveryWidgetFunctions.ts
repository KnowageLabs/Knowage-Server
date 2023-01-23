import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard"
import { IDiscoveryWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/DashboardDiscoveryWidget"
import * as  tableWidgetDefaultValues from '../tableWidget/TableWidgetDefaultValues'
import * as  discoveryWidgetDefaultValues from './DiscoveryWidgetDefaultValues'
import * as widgetCommonDefaultValues from '../common/WidgetCommonDefaultValues'

export const createNewDiscoveryWidgetSettings = () => {
    return {
        updatable: true,
        clickable: true,
        facets: discoveryWidgetDefaultValues.getDefaultFacetsSettings(),
        search: discoveryWidgetDefaultValues.getDefaultSearchSettings(),
        configuration: {
            exports: tableWidgetDefaultValues.getDefaultExportsConfiguration(),
            customMessages: tableWidgetDefaultValues.getDefaultCustomMessages()
        },
        interactions: {
            crossNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
            link: widgetCommonDefaultValues.getDefaultLinks(),
            preview: widgetCommonDefaultValues.getDefaultPreview(),
        },
        pagination: tableWidgetDefaultValues.getDefaultPagination(),
        style: {
            title: widgetCommonDefaultValues.getDefaultTitleStyle(),
            borders: widgetCommonDefaultValues.getDefaultBordersStyle(),
            columns: tableWidgetDefaultValues.getDefaultColumnStyles(),
            padding: widgetCommonDefaultValues.getDefaultPaddingStyle(),
            rows: tableWidgetDefaultValues.getDefaultRowsStyle(),
            shadows: widgetCommonDefaultValues.getDefaultShadowsStyle(),
            background: widgetCommonDefaultValues.getDefaultBackgroundStyle()
        },
        tooltips: tableWidgetDefaultValues.getDefaultTooltips(),
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IDiscoveryWidgetSettings
}

export const addColumnToDiscoveryWidgetModel = (widgetModel: IWidget, column: IWidgetColumn) => {
    widgetModel.settings.facets.columns.push(column.columnName)
    widgetModel.settings.search.columns.push(column.columnName)
}

export const removeColumnFromDiscoveryWidgetModel = (widgetModel: IWidget, column: IWidgetColumn) => {
    removeColumnNameFromStringArray(widgetModel.settings.facets.columns, column.columnName)
    removeColumnNameFromStringArray(widgetModel.settings.search.columns, column.columnName)
}

const removeColumnNameFromStringArray = (array: string[], columnName: string) => {
    const index = array.findIndex((element: string) => element === columnName)
    if (index !== -1) array.splice(index, 1)
}