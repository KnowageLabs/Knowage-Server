import { IWidget, ITableWidgetColumnGroup, IDataset, IWidgetCrossNavigation, IVariable, IDashboardDriver } from '../../Dashboard'

export const getColumnGroup = (propWidget: IWidget, col: ITableWidgetColumnGroup) => {
    const modelGroups = propWidget.settings.configuration.columnGroups.groups
    if (propWidget.settings.configuration.columnGroups.enabled && modelGroups && modelGroups.length > 0) {
        for (const k in modelGroups) {
            if (modelGroups[k].columns.includes(col.id)) {
                return modelGroups[k]
            }
        }
    } else return false
}

export const getWidgetStyleByType = (propWidget: IWidget, styleType: string) => {
    const styleSettings = propWidget?.settings.style[styleType]
    if (styleSettings?.enabled) {
        const styleString = Object.entries(styleSettings.properties ?? styleSettings)
            .map(([k, v]) => `${k}:${v}`)
            .join(';')
        return styleString + ';'
    } else return ''
}

export const stringifyStyleProperties = (properties: object) => {
    const styleString = Object.entries(properties)
        .map(([k, v]) => `${k}:${v}`)
        .join(';')
    return styleString + ';'
}

export const getWidgetStyleByTypeWithoutValidation = (propWidget: IWidget, styleType: string) => {
    const styleSettings = propWidget.settings.style[styleType]
    const styleString = Object.entries(styleSettings.properties ?? styleSettings)
        .map(([k, v]) => `${k}:${v}`)
        .join(';')
    return styleString + ';'
}

export const getColumnConditionalStyles = (propWidget: IWidget, colId: string, valueToCompare: any, returnString?: boolean, variables?: IVariable[], drivers?: IDashboardDriver[]) => {
    const conditionalStyles = propWidget.settings.conditionalStyles
    let styleString = null as any

    const columnConditionalStyles = conditionalStyles.conditions.filter((condition) => condition.target.includes(colId) || condition.condition.formula)
    const columnName = propWidget.columns.find((column) => column.id === colId)?.columnName

    if (columnConditionalStyles.length > 0) {
        for (let i = 0; i < columnConditionalStyles.length; i++) {
            if (
                (columnConditionalStyles[i].condition.formula && isFormulaConditionMet(columnConditionalStyles[i].condition.formula, valueToCompare, columnName, variables, drivers)) ||
                (!columnConditionalStyles[i].condition.formula && isConditionMet(columnConditionalStyles[i].condition, valueToCompare))
            ) {
                if (columnConditionalStyles[i].applyToWholeRow && !returnString) {
                    styleString = columnConditionalStyles[i].properties
                } else if (returnString) {
                    styleString = Object.entries(columnConditionalStyles[i].properties)
                        .map(([k, v]) => `${k}:${v}`)
                        .join(';')
                } else if (!returnString) {
                    styleString = columnConditionalStyles[i].properties
                }
                break
            }
        }
    }
    return styleString
}

const isFormulaConditionMet = (formula, valueToCompare, columnName, variables?: IVariable[], drivers?: IDashboardDriver[]) => {
    const formattedFormula = replacePlaceholders(formula, valueToCompare, false, columnName, variables, drivers)
    return eval(formattedFormula)
}

const replacePlaceholders = (text, data, skipAdapting, columnName, variables?: IVariable[], drivers?: IDashboardDriver[]) => {
    function adaptToType(value) {
        if (skipAdapting) return value
        else return isNaN(value) ? '"' + value + '"' : value
    }
    // variables
    text = text.replace(/\$V\{([a-zA-Z0-9_\-.]+)\}/g, (match, variableName) => {
        if (variables && variables.length > 0) {
            const dashboardVariable = variables.find((variable) => variable.name === variableName)
            if (dashboardVariable) return adaptToType(dashboardVariable.value)
        }
    })
    // fields
    text = text.replace(/\$F\{([a-zA-Z0-9_\-.]+)\}/g, (match, field) => {
        if (field === columnName) return adaptToType(data)
    })
    // parameters/drivers
    text = text.replace(/\$P\{([a-zA-Z0-9_\-.]+)\}/g, (match, parameterName) => {
        if (drivers && drivers.length > 0) {
            const dashboardVariable = drivers.find((driver) => driver.urlName === parameterName)
            if (dashboardVariable) return adaptToType(dashboardVariable.value)
        }
    })

    return text
}

export const isConditionMet = (condition, valueToCompare) => {
    let fullfilledCondition = false
    switch (condition.operator) {
        case '==':
            fullfilledCondition = valueToCompare == condition.value
            break
        case '>=':
            fullfilledCondition = valueToCompare >= condition.value
            break
        case '<=':
            fullfilledCondition = valueToCompare <= condition.value
            break
        case 'IN':
            fullfilledCondition = condition.value.split(',').indexOf(valueToCompare) != -1
            break
        case '>':
            fullfilledCondition = valueToCompare > condition.value
            break
        case '<':
            fullfilledCondition = valueToCompare < condition.value
            break
        case '!=':
            fullfilledCondition = valueToCompare != condition.value
            break
    }
    return fullfilledCondition
}

export const createNewTableSelection = (value: (string | number)[], columnName: string, widget: IWidget, datasets: IDataset[]) => {
    return { datasetId: widget.dataset as number, datasetLabel: getDatasetLabel(widget.dataset as number, datasets) as string, columnName: columnName, value: value, aggregated: false, timestamp: new Date().getTime() }
}

const getDatasetLabel = (datasetId: number, datasets: IDataset[]) => {
    const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
    return index !== -1 ? datasets[index].label : ''
}

export const isCrossNavigationActive = (tableNode: any, crossNavigationOptions: IWidgetCrossNavigation) => {
    if (!crossNavigationOptions.enabled) return false
    if (crossNavigationOptions.type === 'singleColumn' && (!crossNavigationOptions.column || tableNode.colDef?.colId !== crossNavigationOptions.column)) return false
    if (crossNavigationOptions.type === 'icon' && tableNode.colDef?.colId !== 'iconColumn') return false
    return true
}

export const formatRowDataForCrossNavigation = (tableNode: any, dataToShow: any) => {
    const columnDefs = tableNode.columnApi?.columnModel?.columnDefs
    const rowData = tableNode.node.data
    if (!columnDefs || !rowData) return {}
    const formattedRow = {}
    columnDefs.forEach((columnDef: any) => (formattedRow[columnDef.columnName] = { value: rowData[columnDef.field], type: getColumnType(columnDef.field, dataToShow) }))
    return formattedRow
}

export const getFormattedClickedValueForCrossNavigation = (tableNode: any, dataToShow: any) => {
    const type = tableNode.colDef?.colId === 'iconColumn' ? 'icon' : getColumnType(tableNode.colDef?.field, dataToShow)
    return { value: tableNode.value, type: type }
}

const getColumnType = (columnField: string, dataToShow: any) => {
    if (!dataToShow.metaData || !dataToShow.metaData.fields) return ''
    const index = dataToShow.metaData.fields.findIndex((field: any) => field.name === columnField)
    return index !== -1 ? dataToShow.metaData.fields[index].type : ''
}

export const addIconColumn = (columns: any[], propWidget: IWidget, HeaderRenderer: any, CellRenderer: any) => {
    const crossNavigationOptions = propWidget.settings.interactions.crossNavigation as IWidgetCrossNavigation
    if (crossNavigationOptions.enabled && crossNavigationOptions.type === 'icon')
        columns.push({
            colId: 'iconColumn',
            valueGetter: `node.rowIndex + 1`,
            headerName: '',
            pinned: 'right',
            width: 55,
            sortable: false,
            filter: false,
            headerComponent: HeaderRenderer,
            headerComponentParams: { propWidget: propWidget },
            cellRenderer: CellRenderer,
            cellRendererParams: { colId: 'iconColumn', propWidget: propWidget }
        })
}
