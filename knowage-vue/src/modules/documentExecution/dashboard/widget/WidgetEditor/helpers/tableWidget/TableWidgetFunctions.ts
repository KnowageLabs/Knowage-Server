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
    removeColumnFromSubmodel(column, widgetModel.settings.visualization.types, 'target', 'columnRemovedFromVisibilityTypes', true)
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


// TODO - Remove methods
const removeColumnFromHeadersConfiguration = (widgetModel: IWidget, column: IWidgetColumn) => {
    let removed = false
    const headersModel = widgetModel.settings.configuration.headers
    for (let i = headersModel.custom.rules.length - 1; i >= 0; i--) {
        for (let j = headersModel.custom.rules[i].target.length; j >= 0; j--) {
            const tempTarget = headersModel.custom.rules[i].target[j]
            if (column.id === tempTarget) {
                headersModel.custom.rules[i].target.splice(j, 1)
                removed = true
            }
        }
        if (headersModel.custom.rules[i].target.length === 0) headersModel.custom.rules.splice(i, 1)
    }
    if (removed) emitter.emit('headersColumnRemoved')
}

const removeColumnFromColumnGroups = (widgetModel: IWidget, column: IWidgetColumn) => {
    let removed = false
    const columnGroupsModel = widgetModel.settings.configuration.columnGroups
    for (let i = columnGroupsModel.groups.length - 1; i >= 0; i--) {
        for (let j = columnGroupsModel.groups[i].columns.length; j >= 0; j--) {
            const tempColumn = columnGroupsModel.groups[i].columns[j]
            if (column.id === tempColumn) {
                columnGroupsModel.groups[i].columns.splice(j, 1)
                removed = true
            }
        }
    }
    if (removed) emitter.emit('columnRemovedFromColumnGroups')
}

const removeColumnFromVisualizationType = (widgetModel: IWidget, column: IWidgetColumn) => {
    let removed = false
    const visualizationTypes = widgetModel.settings.visualization.types
    for (let i = visualizationTypes.length - 1; i >= 1; i--) {
        for (let j = visualizationTypes[i].target.length; j >= 0; j--) {
            const tempTarget = visualizationTypes[i].target[j]
            if (column.id === tempTarget) {
                (visualizationTypes[i].target as string[]).splice(j, 1)
                removed = true;
            }
        }
        if (visualizationTypes[i].target.length === 0) visualizationTypes.splice(i, 1)
    }
    if (removed) emitter.emit('columnRemovedFromVisibilityTypes')
}

const removeColumnFromVisibilityConditions = (widgetModel: IWidget, column: IWidgetColumn) => {
    let removed = false
    const visibilityConditions = widgetModel.settings.visualization.visibilityConditions
    for (let i = visibilityConditions.conditions.length - 1; i >= 0; i--) {
        for (let j = visibilityConditions[i].target.length; j >= 0; j--) {
            const tempTarget = visibilityConditions[i].target[j]
            if (column.id === tempTarget) {
                visibilityConditions[i].target.splice(j, 1)
                removed = true
            }
        }
        if (visibilityConditions[i].target.length === 0) visibilityConditions.conditions.splice(i, 1)
    }
    if (removed) emitter.emit('columnRemovedFromVisibilityConditions')
}

const removeColumnFromColumnStyle = (widgetModel: IWidget, column: IWidgetColumn) => {
    let removed = false
    const columnStyles = widgetModel.settings.style.columns
    for (let i = columnStyles.length - 1; i >= 1; i--) {
        for (let j = columnStyles[i].target.length; j >= 0; j--) {
            const tempTarget = columnStyles[i].target[j]
            if (column.id === tempTarget) {
                (columnStyles[i].target as string[]).splice(j, 1)
                removed = true
            }
        }
        if (columnStyles[i].target.length === 0) columnStyles.splice(i, 1)
    }
    if (removed) emitter.emit('columnRemovedFromColumnStyle')
}

const removeColumnFromConditionalStyles = (widgetModel: IWidget, column: IWidgetColumn) => {
    let removed = false
    const conditionalStyles = widgetModel.settings.conditionalStyles
    for (let i = conditionalStyles.conditions.length - 1; i >= 0; i--) {
        if (column.id === conditionalStyles[i].target) {
            conditionalStyles.conditions.splice(i, 1)
            removed = true
        }
    }
    if (removed) emitter.emit('columnRemovedFromConditionalStyles')
}

const removeColumnFromTooltips = (widgetModel: IWidget, column: IWidgetColumn) => {
    let removed = false
    const tooltips = widgetModel.settings.tooltips
    for (let i = tooltips.length - 1; i >= 1; i--) {
        for (let j = tooltips[i].target.length; j >= 0; j--) {
            const tempTarget = tooltips[i].target[j]
            if (column.id === tempTarget) {
                (tooltips[i].target as string[]).splice(j, 1)
                removed = true
            }
        }
        if (tooltips[i].target.length === 0) tooltips.splice(i, 1)
    }
    if (removed) emitter.emit('columnRemovedFromTooltips')
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

