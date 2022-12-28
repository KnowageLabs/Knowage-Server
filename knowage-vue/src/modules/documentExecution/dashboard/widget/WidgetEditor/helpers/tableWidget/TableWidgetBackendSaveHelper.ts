import { ITableWidgetColumnGroups, ITableWidgetConditionalStyles, ITableWidgetConfiguration, IWidgetCrossNavigation, ITableWidgetHeaders, IWidgetInteractions, IWidgetSelection, ITableWidgetSettings, ITableWidgetVisualization, IWidget, IWidgetColumn } from '../../../../Dashboard'

const columnIdNameMap = {}

export function formatTableWidgetForSave(widget: IWidget) {
    if (!widget) return

    loadColumnIdNameMap(widget)
    formatTableSelectedColumns(widget.columns)
    formatTableSettings(widget.settings)
}

function formatTableSelectedColumns(columns: IWidgetColumn[]) {
    if (!columns) return
    columns.forEach((column: IWidgetColumn) => {
        delete column.id
    })
}

const loadColumnIdNameMap = (widget: IWidget) => {
    widget.columns?.forEach((column: IWidgetColumn) => {
        if (column.id) columnIdNameMap[column.id] = column.columnName
    })
}

const getColumnName = (columnId: string) => {
    return columnId ? columnIdNameMap[columnId] : ''
}

const formatTableSettings = (widgetSettings: ITableWidgetSettings) => {
    formatTableWidgetConfiguration(widgetSettings.configuration)
    formatTableWidgetVisualisation(widgetSettings.visualization)
    formatTableWidgetConditionalStyle(widgetSettings.conditionalStyles)
    formatTableInteractions(widgetSettings.interactions)
}

const formatTableWidgetConfiguration = (widgetConfiguration: ITableWidgetConfiguration) => {
    formatRowsConfiguration(widgetConfiguration)
    formatHeadersConfiguration(widgetConfiguration)
    formatColumnGroups(widgetConfiguration)
}

const formatRowsConfiguration = (widgetConfiguration: ITableWidgetConfiguration) => {
    if (!widgetConfiguration.rows) return
    widgetConfiguration.rows.rowSpan.column = getColumnName(widgetConfiguration.rows.rowSpan.column)
}

const formatHeadersConfiguration = (widgetConfiguration: ITableWidgetConfiguration) => {
    if (!widgetConfiguration.headers) return
    formatHeaderConfigurationRules(widgetConfiguration.headers)
}

const formatHeaderConfigurationRules = (configurationHeaders: ITableWidgetHeaders) => {
    for (let i = 0; i < configurationHeaders.custom.rules.length; i++) {
        const tempRule = configurationHeaders.custom.rules[i]
        const formattedRuleColumns = [] as string[]
        for (let j = 0; j < tempRule.target.length; j++) {
            formattedRuleColumns.push(getColumnName(tempRule.target[j]))
        }
        tempRule.target = formattedRuleColumns
        delete tempRule.variablePivotDatasetOptions
    }
}

const formatColumnGroups = (widgetConfiguration: ITableWidgetConfiguration) => {
    if (!widgetConfiguration.columnGroups) return
    formatColumnGroupsColumnIdToName(widgetConfiguration.columnGroups)
}

const formatTableWidgetVisualisation = (widgetVisualization: ITableWidgetVisualization) => {
    formatVisualizationTypes(widgetVisualization)
    formatVisibilityConditions(widgetVisualization)
}

const formatVisualizationTypes = (widgetVisualization: ITableWidgetVisualization) => {
    for (let i = 1; i < widgetVisualization.visualizationTypes.types.length; i++) {
        const tempVisualization = widgetVisualization.visualizationTypes.types[i]
        const formattedRuleColumns = [] as string[]
        for (let j = 0; j < tempVisualization.target.length; j++) {
            formattedRuleColumns.push(getColumnName(tempVisualization.target[j]))
        }
        tempVisualization.target = formattedRuleColumns
    }
}
const formatVisibilityConditions = (widgetVisualization: ITableWidgetVisualization) => {
    for (let i = 0; i < widgetVisualization.visibilityConditions.conditions.length; i++) {
        const tempCondition = widgetVisualization.visibilityConditions.conditions[i]
        const formattedRuleColumns = [] as string[]
        for (let j = 0; j < tempCondition.target.length; j++) {
            formattedRuleColumns.push(getColumnName(tempCondition.target[j]))
        }
        tempCondition.target = formattedRuleColumns
        delete tempCondition.condition.variablePivotDatasetOptions
    }
}

const formatTableWidgetConditionalStyle = (widgetConditionalStyles: ITableWidgetConditionalStyles) => {
    for (let i = 0; i < widgetConditionalStyles.conditions.length; i++) {
        const tempCondition = widgetConditionalStyles.conditions[i]
        delete tempCondition.condition.variablePivotDatasetOptions
    }
}

const formatColumnGroupsColumnIdToName = (columnGroupsConfiguration: ITableWidgetColumnGroups) => {
    for (let i = 0; i < columnGroupsConfiguration.groups.length; i++) {
        const tempColumnGroup = columnGroupsConfiguration.groups[i]
        const formattedColumnGroupColumns = [] as string[]
        for (let j = 0; j < tempColumnGroup.columns.length; j++) {
            formattedColumnGroupColumns.push(getColumnName(tempColumnGroup.columns[j]))
        }
        tempColumnGroup.columns = formattedColumnGroupColumns
    }
}

const formatTableInteractions = (widgetInteractions: IWidgetInteractions) => {
    formatSelection(widgetInteractions.selection as IWidgetSelection)
    formatCrossNavigation(widgetInteractions.crossNavigation)
}

const formatSelection = (selection: IWidgetSelection) => {
    if (selection.modalColumn) selection.modalColumn = getColumnName(selection.modalColumn)
}

const formatCrossNavigation = (crossNavigation: IWidgetCrossNavigation) => {
    if (crossNavigation.column) crossNavigation.column = getColumnName(crossNavigation.column)
}
