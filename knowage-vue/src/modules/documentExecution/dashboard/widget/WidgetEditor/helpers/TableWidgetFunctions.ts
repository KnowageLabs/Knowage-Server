import { IWidget, IWidgetColumn, IIcon, ITableWidgetSettings, ITableWidgetConfiguration, ITableWidgetHeaders, ITableWidgetColumnGroups, ITableWidgetColumnGroup } from "../../../Dashboard"
import { formatRGBColor } from './WidgetEditorHelpers'
import { emitter } from '../../../DashboardHelpers'
import descriptor from '../WidgetEditorDescriptor.json'
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

export function formatTableWidgetForSave(widget: IWidget) {
    if (!widget) return

    formatTableSettings(widget.settings, widget.columns)
    formatTableSelectedColumns(widget.columns)
    // formatRowHeaderSettings(widget)
    // formatRowStyleSettings(widget)
    // formatBorderSettings(widget)
}

const formatTableSettings = (widgetSettings: ITableWidgetSettings, widgetColumns: IWidgetColumn[]) => {
    formatTableWidgetConfiguration(widgetSettings.configuration, widgetColumns)
}

const formatTableWidgetConfiguration = (widgetConfiguration: ITableWidgetConfiguration, widgetColumns: IWidgetColumn[]) => {
    // formatRowsConfiguration(widgetConfiguration, widgetColumns) // TODO - BE SAVE
    formatHeadersConfiguration(widgetConfiguration, widgetColumns) // TODO - BE SAVE
    formatSummaryRows(widgetConfiguration)
    formatColumnGroups(widgetConfiguration, widgetColumns)
}


// TODO - BE SAVE
const formatRowsConfiguration = (widgetConfiguration: ITableWidgetConfiguration, widgetColumns: IWidgetColumn[]) => {
    if (!widgetConfiguration.rows) return
    console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> TEEEEEEST COLUMNS: ", widgetColumns)

    widgetConfiguration.rows.rowSpan.column = getColumnName(widgetConfiguration.rows.rowSpan.column, widgetColumns)

    console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> TEEEEEEST: ", widgetConfiguration.rows)
}


const formatHeadersConfiguration = (widgetConfiguration: ITableWidgetConfiguration, widgetColumns: IWidgetColumn[]) => {
    if (!widgetConfiguration.headers) return
    if (!widgetConfiguration.headers.custom.enabled) {
        widgetConfiguration.headers.custom.rules = []
        return
    }

    // formatHeaderConfigurationRules(widgetConfiguration.headers, widgetColumns) // TODO - BE SAVE
}

const formatHeaderConfigurationRules = (configurationHeaders: ITableWidgetHeaders, widgetColumns: IWidgetColumn[]) => {
    for (let i = 0; i < configurationHeaders.custom.rules.length; i++) {
        const tempRule = configurationHeaders.custom.rules[i]
        const formattedRuleColumns = [] as string[]
        for (let j = 0; j < tempRule.target.length; j++) {
            formattedRuleColumns.push(getColumnName(tempRule.target[j], widgetColumns))
        }
        tempRule.target = formattedRuleColumns
    }

}

const formatColumnGroups = (widgetConfiguration: ITableWidgetConfiguration, widgetColumns: IWidgetColumn[]) => {
    console.log("FORMAT COLUMN GROUPS: ", widgetConfiguration)
    if (!widgetConfiguration.columnGroups) return
    if (!widgetConfiguration.columnGroups.enabled) {
        widgetConfiguration.columnGroups.groups = []
        return
    }

    // formatColumnGroupsColumnIdToName(widgetConfiguration.columnGroups, widgetColumns) TODO - BE SAVE

}

const formatColumnGroupsColumnIdToName = (columnGroupsConfiguration: ITableWidgetColumnGroups, widgetColumns: IWidgetColumn[]) => {
    for (let i = 0; i < columnGroupsConfiguration.groups.length; i++) {
        const tempColumnGroup = columnGroupsConfiguration.groups[i]
        const formattedColumnGroupColumns = [] as string[]
        for (let j = 0; j < tempColumnGroup.columns.length; j++) {
            formattedColumnGroupColumns.push(getColumnName(tempColumnGroup.columns[j], widgetColumns))
        }
        tempColumnGroup.columns = formattedColumnGroupColumns
    }
}

const formatSummaryRows = (widgetConfiguration: ITableWidgetConfiguration) => {
    if (!widgetConfiguration.summaryRows) return
    if (!widgetConfiguration.summaryRows.enabled) {
        widgetConfiguration.summaryRows.style.pinnedOnly = false
        widgetConfiguration.summaryRows.list = []
    }
}

const getColumnName = (columnsId: string, widgetColumns: IWidgetColumn[]) => {
    const index = widgetColumns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === columnsId)
    return index !== -1 ? widgetColumns[index].columnName : ''
}

function formatTableSelectedColumns(columns: IWidgetColumn[]) {
    if (!columns) return
    columns.forEach((column: IWidgetColumn) => {
        // delete column.id
        formatColumnFilter(column)
        // formatColumnTooltipSettings(column)
    })
}

const formatColumnFilter = (column: IWidgetColumn) => {
    if (!column.filter) return
    if (!column.filter.enabled) return delete column.filter
    if (column.filter.operator !== 'range') delete column.filter.value2
}

function formatColumnTooltipSettings(column: IWidgetColumn) {
    if (column.enableTooltip) {
        column.style.tooltip.precision = +column.style.tooltip.precision
    } else {
        if (!column.style) return // TODO
        column.style.tooltip = { prefix: '', suffix: '', precision: 0 }
        column.style.enableCustomHeaderTooltip = false
        column.style.customHeaderTooltip = ''
    }
}




//#region ===================== Remove Column ====================================================
export const removeColumnFromModel = (widgetModel: IWidget, column: IWidgetColumn) => {
    removeColumnFromRows(widgetModel, column)
    removeColumnFromHeadersConfiguration(widgetModel, column)
    removeColumnFromColumnGroups(widgetModel, column)
    removeColumnFromVisualizationType(widgetModel, column)
    removeColumnFromVisibilityConditions(widgetModel, column)
    removeColumnFromColumnStyle(widgetModel, column)
    removeColumnFromConditionalStyles(widgetModel, column)
    removeColumnFromTooltips(widgetModel, column)
}

const removeColumnFromRows = (widgetModel: IWidget, column: IWidgetColumn) => {
    if (column.id === widgetModel.settings.configuration.rows.rowSpan.column) {
        widgetModel.settings.configuration.rows.rowSpan.column = ''
        emitter.emit('columnRemovedFromRows')
    }
}

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
                visualizationTypes[i].target.splice(j, 1)
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
    for (let i = visibilityConditions.length - 1; i >= 0; i--) {
        for (let j = visibilityConditions[i].target.length; j >= 0; j--) {
            const tempTarget = visibilityConditions[i].target[j]
            if (column.id === tempTarget) {
                visibilityConditions[i].target.splice(j, 1)
                removed = true
            }
        }
        if (visibilityConditions[i].target.length === 0) visibilityConditions.splice(i, 1)
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
                columnStyles[i].target.splice(j, 1)
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
    for (let i = conditionalStyles.length - 1; i >= 0; i--) {
        if (column.id === conditionalStyles[i].target) {
            conditionalStyles.splice(i, 1)
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
                tooltips[i].target.splice(j, 1)
                removed = true
            }
        }
        if (tooltips[i].target.length === 0) tooltips.splice(i, 1)
    }
    if (removed) emitter.emit('columnRemovedFromTooltips')
}

export const removeColumnGroupFromModel = (widgetModel: IWidget, columnGroup: ITableWidgetColumnGroup) => {
    let removed = false
    for (let i = widgetModel.settings.style.columnGroups.length - 1; i >= 0; i--) {
        for (let j = widgetModel.settings.style.columnGroups[i].target.length; j >= 0; j--) {
            const tempTarget = widgetModel.settings.style.columnGroups[i].target[j]
            console.log(columnGroup.id + ' === ' + tempTarget)
            if (columnGroup.id === tempTarget) {
                widgetModel.settings.style.columnGroups[i].target.splice(j, 1)
                removed = true
            }
        }
        if (widgetModel.settings.style.columnGroups[i].target.length === 0) widgetModel.settings.style.columnGroups.splice(i, 1)
    }
    if (removed) emitter.emit('columnGroupRemoved')
}

export default removeColumnFromModel
//#endregion ================================================================================================

