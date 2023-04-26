import { IWidget, IWidgetColumn, ITableWidgetColumnGroup, IWidgetInteractionParameter, ITableWidgetSettings } from "../../../../Dashboard"
import { emitter } from '../../../../DashboardHelpers'
import * as  tableWidgetDefaultValues from './TableWidgetDefaultValues'
import * as widgetCommonDefaultValues from '../common/WidgetCommonDefaultValues'

export const createNewTableWidgetSettings = () => {
    return {
        sortingColumn: '',
        sortingOrder: '',
        updatable: true,
        clickable: true,
        conditionalStyles: tableWidgetDefaultValues.getDefaultConditionalStyles(),
        configuration: {
            columnGroups: tableWidgetDefaultValues.getDefaultColumnGroups(),
            exports: tableWidgetDefaultValues.getDefaultExportsConfiguration(),
            headers: tableWidgetDefaultValues.getDefaultHeadersConfiguration(),
            rows: tableWidgetDefaultValues.getDefaultRowsConfiguration(),
            summaryRows: tableWidgetDefaultValues.getDefaultSummaryRowsConfiguration(),
            customMessages: tableWidgetDefaultValues.getDefaultCustomMessages()
        },
        interactions: {
            crossNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
            link: widgetCommonDefaultValues.getDefaultLinks(),
            preview: widgetCommonDefaultValues.getDefaultPreview(),
            selection: tableWidgetDefaultValues.getDefaultSelection()
        },
        pagination: tableWidgetDefaultValues.getDefaultPagination(),
        style: {
            title: widgetCommonDefaultValues.getDefaultTitleStyle(),
            borders: widgetCommonDefaultValues.getDefaultBordersStyle(),
            columns: tableWidgetDefaultValues.getDefaultColumnStyles(),
            columnGroups: tableWidgetDefaultValues.getDefaultColumnStyles(),
            headers: tableWidgetDefaultValues.getDefaultHeadersStyle(),
            padding: widgetCommonDefaultValues.getDefaultPaddingStyle(),
            rows: tableWidgetDefaultValues.getDefaultRowsStyle(),
            shadows: widgetCommonDefaultValues.getDefaultShadowsStyle(),
            summary: tableWidgetDefaultValues.getDefualtSummryStyle(),
            background: widgetCommonDefaultValues.getDefaultBackgroundStyle()
        },
        tooltips: tableWidgetDefaultValues.getDefaultTooltips(),
        visualization: tableWidgetDefaultValues.getDefaultVisualizations(),
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as ITableWidgetSettings
}

export const removeColumnFromTableWidgetModel = (widgetModel: IWidget, column: IWidgetColumn) => {
    removeColumnFromRows(widgetModel, column)
    removeColumnFromSubmodel(column, widgetModel.settings.configuration.headers.custom.rules, 'target', 'headersColumnRemoved', false)
    removeColumnFromSubmodel(column, widgetModel.settings.configuration.columnGroups.groups, 'columns', 'columnRemovedFromColumnGroups', false)
    removeColumnFromSubmodel(column, widgetModel.settings.visualization.visualizationTypes.types, 'target', 'columnRemovedFromVisibilityTypes', true)
    removeColumnFromSubmodel(column, widgetModel.settings.visualization.visibilityConditions.conditions, 'target', 'columnRemovedFromVisibilityConditions', false)
    removeColumnFromSubmodel(column, widgetModel.settings.style.columns.styles, 'target', 'columnRemovedFromColumnStyle', true)
    removeColumnFromSubmodel(column, widgetModel.settings.conditionalStyles.conditions, 'target', 'columnRemovedFromConditionalStyles', false)
    removeColumnFromSubmodel(column, widgetModel.settings.tooltips, 'target', 'columnRemovedFromTooltips', true)
    removeColumnFromCrossNavigation(widgetModel, column)
}

const removeColumnFromRows = (widgetModel: IWidget, column: IWidgetColumn) => {
    if (column.id === widgetModel.settings.configuration.rows.rowSpan.column) {
        widgetModel.settings.configuration.rows.rowSpan.column = ''
        emitter.emit('columnRemovedFromRows')
    }
}

export const removeColumnFromSubmodel = (column: IWidgetColumn, array: any[], subProperty: string, eventToEmit: string, allColumnsOption: boolean) => {
    let removed = false
    for (let i = array.length - 1; i >= (allColumnsOption ? 1 : 0); i--) {
        for (let j = (array[i][subProperty] as string[]).length; j >= 0; j--) {
            const tempTarget = array[i][subProperty][j]
            if (column.id === tempTarget) {
                (array[i][subProperty] as string[]).splice(j, 1)
                removed = true
            }
        }
        if ((array[i][subProperty] as string[]).length === 0) array.splice(i, 1)
    }
    if (removed) emitter.emit(eventToEmit)
}

const removeColumnFromCrossNavigation = (widgetModel: IWidget, column: IWidgetColumn) => {
    const crossNavigation = widgetModel.settings.interactions.crossNavigation
    if (crossNavigation.column === column.id) {
        crossNavigation.enabled = false;
        crossNavigation.parameters.forEach((parameter: IWidgetInteractionParameter) => {
            parameter.enabled = false
            if (parameter.column === column.columnName) parameter.column = ''
        })
        emitter.emit('columnRemovedFromCrossNavigation')
    }
}

export const removeColumnGroupFromModel = (widgetModel: IWidget, columnGroup: ITableWidgetColumnGroup) => {
    let removed = false
    for (let i = widgetModel.settings.style.columnGroups.styles.length - 1; i >= 0; i--) {
        for (let j = widgetModel.settings.style.columnGroups[i].target.length; j >= 0; j--) {
            const tempTarget = widgetModel.settings.style.columnGroups[i].target[j]
            if (columnGroup.id === tempTarget) {
                (widgetModel.settings.style.columnGroups[i].target as string[]).splice(j, 1)
                removed = true
            }
        }
        if (widgetModel.settings.style.columnGroups[i].target.length === 0) widgetModel.settings.style.columnGroups.styles.splice(i, 1)
    }
    if (removed) emitter.emit('columnGroupRemoved')
}

export default removeColumnFromTableWidgetModel


