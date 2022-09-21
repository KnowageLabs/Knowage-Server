import { ITableWidgetColumnGroups, ITableWidgetConfiguration, ITableWidgetHeaders, ITableWidgetSettings, IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard"
import deepcopy from 'deepcopy'

const columnIdNameMap = {}

export function formatTableWidgetForSave(widget: IWidget) {
    // TODO - CHANGE WHEN BE IS DONE
    const tempWidget = deepcopy(widget)

    if (!tempWidget) return
    loadColumnIdNameMap(tempWidget)
    formatTableSettings(tempWidget.settings, tempWidget.columns)
    formatTableSelectedColumns(tempWidget.columns)
    // formatRowHeaderSettings(widget)
    // formatRowStyleSettings(widget)
    // formatBorderSettings(widget)

    console.log(">>>>>>>>>>>>>>>>>>>>>>>>>> BE SAVE - FORMATTED WIDGET: ", tempWidget)
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
    // formatRowsConfiguration(widgetConfiguration, widgetColumns) // TODO - BE SAVE
    formatHeadersConfiguration(widgetConfiguration, widgetColumns) // TODO - BE SAVE
    formatSummaryRows(widgetConfiguration)
    formatColumnGroups(widgetConfiguration, widgetColumns)
}


// TODO - BE SAVE
const formatRowsConfiguration = (widgetConfiguration: ITableWidgetConfiguration, widgetColumns: IWidgetColumn[]) => {
    if (!widgetConfiguration.rows) return
    console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> TEEEEEEST COLUMNS: ", widgetColumns)

    widgetConfiguration.rows.rowSpan.column = getColumnName(widgetConfiguration.rows.rowSpan.column)

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
            formattedRuleColumns.push(getColumnName(tempRule.target[j]))
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

function formatTableSelectedColumns(columns: IWidgetColumn[]) {
    if (!columns) return
    columns.forEach((column: IWidgetColumn) => {
        // delete column.id
        // formatColumnTooltipSettings(column)
    })
}