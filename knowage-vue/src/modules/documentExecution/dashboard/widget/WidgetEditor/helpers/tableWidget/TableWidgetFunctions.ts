import { IWidget, IWidgetColumn, IIcon, ITableWidgetSettings, ITableWidgetConfiguration, ITableWidgetHeaders, ITableWidgetColumnGroups, ITableWidgetColumnGroup, ITableWidgetParameter } from "../../../../Dashboard"
import { emitter } from '../../../../DashboardHelpers'
import cryptoRandomString from 'crypto-random-string'

export const createNewWidgetColumn = (eventData: any) => {
    const tempColumn = {
        id: cryptoRandomString({ length: 16, type: 'base64' }),
        columnName: eventData.name,
        alias: eventData.alias,
        type: eventData.type,
        fieldType: eventData.fieldType,
        filter: {}
    } as IWidgetColumn
    if (tempColumn.fieldType === 'MEASURE') tempColumn.aggregation = 'SUM'
    return tempColumn
}

//#region ===================== Remove Column ====================================================
export const removeColumnFromModel = (widgetModel: IWidget, column: IWidgetColumn) => {
    removeColumnFromRows(widgetModel, column)
    removeColumnFromSubmodel(column, widgetModel.settings.configuration.headers.custom.rules, 'target', 'headersColumnRemoved', false)
    removeColumnFromSubmodel(column, widgetModel.settings.configuration.columnGroups.groups, 'columns', 'columnRemovedFromColumnGroups', false)
    removeColumnFromSubmodel(column, widgetModel.settings.visualization.visualizationTypes.types, 'target', 'columnRemovedFromVisibilityTypes', true)
    removeColumnFromSubmodel(column, widgetModel.settings.visualization.visibilityConditions.conditions, 'target', 'columnRemovedFromVisibilityConditions', false)
    removeColumnFromSubmodel(column, widgetModel.settings.style.columns, 'target', 'columnRemovedFromColumnStyle', true)
    removeColumnFromSubmodel(column, widgetModel.settings.conditionalStyles.conditions, 'target', 'columnRemovedFromConditionalStyles', false)
    removeColumnFromSubmodel(column, widgetModel.settings.tooltips, 'target', 'columnRemovedFromTooltips', true)
    // removeColumnFromHeadersConfiguration(widgetModel, column)
    // removeColumnFromColumnGroups(widgetModel, column)
    //  removeColumnFromVisualizationType(widgetModel, column)
    // removeColumnFromVisibilityConditions(widgetModel, column)
    // removeColumnFromColumnStyle(widgetModel, column)
    // removeColumnFromConditionalStyles(widgetModel, column)
    // removeColumnFromTooltips(widgetModel, column)
    removeColumnFromCrossNavigation(widgetModel, column)
}

const removeColumnFromRows = (widgetModel: IWidget, column: IWidgetColumn) => {
    if (column.id === widgetModel.settings.configuration.rows.rowSpan.column) {
        widgetModel.settings.configuration.rows.rowSpan.column = ''
        emitter.emit('columnRemovedFromRows')
    }
}

const removeColumnFromSubmodel = (column: IWidgetColumn, array: any[], subProperty: string, eventToEmit: string, allColumnsOption: boolean) => {
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
    const crossNavigation = widgetModel.settings.interactions.crosssNavigation
    if (crossNavigation.column === column.id) {
        crossNavigation.enabled = false;
        crossNavigation.parameters.forEach((parameter: ITableWidgetParameter) => {
            parameter.enabled = false
            console.log(parameter.column + ' === ' + column.columnName)
            if (parameter.column === column.columnName) parameter.column = ''
        })
        emitter.emit('columnRemovedFromCrossNavigation')
    }
}

export const removeColumnGroupFromModel = (widgetModel: IWidget, columnGroup: ITableWidgetColumnGroup) => {
    let removed = false
    for (let i = widgetModel.settings.style.columnGroups.length - 1; i >= 0; i--) {
        for (let j = widgetModel.settings.style.columnGroups[i].target.length; j >= 0; j--) {
            const tempTarget = widgetModel.settings.style.columnGroups[i].target[j]
            console.log(columnGroup.id + ' === ' + tempTarget)
            if (columnGroup.id === tempTarget) {
                (widgetModel.settings.style.columnGroups[i].target as string[]).splice(j, 1)
                removed = true
            }
        }
        if (widgetModel.settings.style.columnGroups[i].target.length === 0) widgetModel.settings.style.columnGroups.splice(i, 1)
    }
    if (removed) emitter.emit('columnGroupRemoved')
}

export default removeColumnFromModel
//#endregion ================================================================================================

