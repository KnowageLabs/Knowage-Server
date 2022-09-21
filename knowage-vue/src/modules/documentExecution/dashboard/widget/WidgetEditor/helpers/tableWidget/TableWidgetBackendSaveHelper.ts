import { ITableWidgetColumnGroups, ITableWidgetConfiguration, ITableWidgetHeaders, ITableWidgetSettings, IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard"
import deepcopy from 'deepcopy'

const columnIdNameMap = {}

export function formatTableWidgetForSave(widget: IWidget) {
    // TODO - CHANGE WHEN BE IS DONE
    const tempWidget = deepcopy(widget)

    if (!tempWidget) return

    loadColumnIdNameMap(tempWidget)
    formatTableSelectedColumns(tempWidget.columns)
    formatTableSettings(tempWidget.settings, tempWidget.columns)


    console.log(">>>>>>>>>>>>>>>>>>>>>>>>>> BE SAVE - FORMATTED WIDGET: ", tempWidget)
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

const formatTableSettings = (widgetSettings: ITableWidgetSettings, widgetColumns: IWidgetColumn[]) => {
    formatTableWidgetConfiguration(widgetSettings.configuration, widgetColumns)
}

const formatTableWidgetConfiguration = (widgetConfiguration: ITableWidgetConfiguration, widgetColumns: IWidgetColumn[]) => {
    formatRowsConfiguration(widgetConfiguration)
    formatHeadersConfiguration(widgetConfiguration)
    formatSummaryRows(widgetConfiguration)
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
    }
}




const formatColumnGroups = (widgetConfiguration: ITableWidgetConfiguration) => {
    if (!widgetConfiguration.columnGroups) return
    if (!widgetConfiguration.columnGroups.enabled) {
        widgetConfiguration.columnGroups.groups = []
        return
    }

    formatColumnGroupsColumnIdToName(widgetConfiguration.columnGroups)
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

const formatSummaryRows = (widgetConfiguration: ITableWidgetConfiguration) => {
    if (!widgetConfiguration.summaryRows) return
    if (!widgetConfiguration.summaryRows.enabled) {
        widgetConfiguration.summaryRows.style.pinnedOnly = false
        widgetConfiguration.summaryRows.list = []
    }
}