import { IWidget } from '../../Dashboard'
import { IPivotTableWidgetConditionalStyle } from '../../interfaces/pivotTable/DashboardPivotTableWidget'
import * as pivotTableDefaultValues from '../../widget/WidgetEditor/helpers/pivotTableWidget/PivotTableDefaultValues'
import { getColumnId } from './PivotTableCompatibilityHelper'

export const getSettingsFromPivotTableWidgetColumns = (formattedWidget: IWidget, widget: any, columnNameIdMap: any) => {
    getSettingsFromMeasureColumns(formattedWidget, widget, columnNameIdMap)

}

const getSettingsFromMeasureColumns = (formattedWidget: IWidget, widget: any, columnNameIdMap: any) => {
    console.log('----- columnNameIdMap: ', columnNameIdMap)
    for (let i = 0; i < widget.content.crosstabDefinition.measures.length; i++) {
        const tempColumn = widget.content.crosstabDefinition.measures[i]
        console.log('----------------- TEMP COLUMN: ', tempColumn)
        // getRowConfigurationFromWidgetColumn(formattedWidget, tempColumn)
        //  getHeaderConfigurationFromWidgetColumn(formattedWidget, tempColumn, formattedDashboardModel)
        // if (tempColumn.group) addColumnToColumnGroup(formattedWidget, tempColumn)
        //  getVisualizationTypeConfigurationsFromColumn(formattedWidget, tempColumn)
        // getVisibilityConditionsFromColumn(formattedWidget, tempColumn, formattedDashboardModel)
        // getStyleFromColumn(formattedWidget, tempColumn, columnNameIdMap)
        getConditionalStyleFromColumn(formattedWidget, tempColumn)
        // getTooltipFromColumn(formattedWidget, tempColumn)
    }
}

// const addColumnToColumnGroup = (formattedWidget: IWidget, tempColumn: any) => {
//     const columnGroups = formattedWidget.settings.configuration.columnGroups.groups
//     const index = columnGroups.findIndex((columnGroup: ITableWidgetColumnGroup) => columnGroup.id === tempColumn.group)
//     if (index !== -1) columnGroups[index].columns.push(getColumnId(tempColumn.name))
// }

// const getVisualizationTypeConfigurationsFromColumn = (formattedWidget: IWidget, tempColumn: any) => {
//     if ((tempColumn.fieldType === 'ATTRIBUTE' && tempColumn.precision !== 0) || tempColumn.style?.prefix || tempColumn.style?.suffix || tempColumn.pinned) {
//         addVisualisationTypeAttributeColumn(formattedWidget, tempColumn)
//     } else if (tempColumn.fieldType === 'MEASURE' && tempColumn.visType) {
//         addVisualisationTypeMeasureColumn(formattedWidget, tempColumn)
//     }
// }

// const getVisibilityConditionsFromColumn = (formattedWidget: IWidget, tempColumn: any, formattedDashboardModel: IDashboard) => {
//     const tempVisibiilityCondition = {
//         target: [getColumnId(tempColumn.name)],
//         hide: tempColumn.style?.hiddenColumn ?? false,
//         hidePdf: tempColumn.style?.hideFromPdf ?? false,
//         condition: {
//             type: 'always'
//         }
//     } as ITableWidgetVisibilityCondition
//     if (tempColumn.variables) {
//         getVisibilityConditionVariable(formattedWidget, tempColumn, tempVisibiilityCondition, formattedDashboardModel)
//     } else if (tempVisibiilityCondition.hide || tempVisibiilityCondition.hidePdf) {
//         formattedWidget.settings.visualization.visibilityConditions.enabled = true
//         formattedWidget.settings.visualization.visibilityConditions.conditions.push(tempVisibiilityCondition)
//     }
// }

// const getVisibilityConditionVariable = (formattedWidget: IWidget, column: any, tempVisibiilityCondition: ITableWidgetVisibilityCondition, formattedDashboardModel: IDashboard) => {
//     const modelVariables = formattedDashboardModel?.configuration?.variables ?? []
//     column.variables.forEach((variable: { action: string; variable: string; condition: string; value: string; key?: string }) => {
//         if (variable.action === 'hide') {
//             const modelVariable = modelVariables.find((tempVariable: IVariable) => tempVariable.name === variable.variable)
//             tempVisibiilityCondition.condition = {
//                 type: 'variable',
//                 variable: variable.variable,
//                 operator: variable.condition,
//                 value: variable.value
//             }
//             if (variable.key) tempVisibiilityCondition.condition.variableKey = variable.key
//             setVisibilityConditionValueFromVariable(tempVisibiilityCondition, modelVariable, variable)
//             formattedWidget.settings.visualization.visibilityConditions.enabled = true
//             addVisibilityConditionToTheModel(tempVisibiilityCondition, formattedWidget)
//         }
//     })
// }

// const setVisibilityConditionValueFromVariable = (tempVisibiilityCondition: ITableWidgetVisibilityCondition, modelVariable: IVariable | undefined, variable: any) => {
//     if (!modelVariable) return
//     switch (modelVariable.type) {
//         case 'static':
//         case 'profile':
//         case 'driver':
//             tempVisibiilityCondition.condition.value = modelVariable.value
//             break
//         case 'dataset':
//             if (modelVariable.column) {
//                 tempVisibiilityCondition.condition.value = modelVariable.value
//             } else {
//                 tempVisibiilityCondition.condition.variableKey = variable.key
//                 tempVisibiilityCondition.condition.variablePivotDatasetOptions = modelVariable.pivotedValues
//                 tempVisibiilityCondition.condition.value = tempVisibiilityCondition.condition.variableKey ? tempVisibiilityCondition.condition.variablePivotDatasetOptions[tempVisibiilityCondition.condition.variableKey] : ''
//             }
//     }
// }

// const addVisibilityConditionToTheModel = (rule: ITableWidgetVisibilityCondition, formattedWidget: IWidget) => {
//     for (let i = 0; i < formattedWidget.settings.visualization.visibilityConditions.conditions.length; i++) {
//         if (formattedWidget.settings.visualization.visibilityConditions.conditions[i].target.includes(rule.target[0])) return
//     }
//     formattedWidget.settings.visualization.visibilityConditions.conditions.push(rule)
// }

// export const getStyleFromColumn = (formattedWidget: IWidget, tempColumn: any, columnNameIdMap: any) => {
//     if (!tempColumn.style) return
//     let hasStyle = false
//     let fields = ['background-color', 'color', 'justify-content', 'font-size', 'font-family', 'font-style', 'font-weight']
//     for (let i = 0; i < fields.length; i++) {
//         if (tempColumn.style.hasOwnProperty(fields[i])) {
//             hasStyle = true
//             break
//         }
//     }

//     if (hasStyle)
//         formattedWidget.settings.style.columns.styles.push({
//             target: [columnNameIdMap[tempColumn.name]],
//             properties: {
//                 width: tempColumn.style.width,
//                 'background-color': tempColumn.style['background-color'] ?? 'rgb(0, 0, 0)',
//                 color: tempColumn.style.color ?? 'rgb(255, 255, 255)',
//                 'justify-content': tempColumn.style['justify-content'] ?? '',
//                 'font-size': tempColumn.style['font-size'] ?? '',
//                 'font-family': tempColumn.style['font-family'] ?? '',
//                 'font-style': tempColumn.style['font-style'] ?? '',
//                 'font-weight': tempColumn.style['font-weight'] ?? ''
//             }
//         })
// }

const getConditionalStyleFromColumn = (formattedWidget: IWidget, tempColumn: any) => {
    if (!tempColumn.ranges || tempColumn.ranges.length === 0) return
    const defaultConditionalStyle = pivotTableDefaultValues.getDefaultConditionalStyle()
    tempColumn.ranges.forEach((range: any) => {
        const tempConditionalStyle = {
            target: getColumnId(tempColumn.id),
            condition: { operator: range.operator, value: range.value },
            properties: {
                'text-align': defaultConditionalStyle.properties['text-align'],
                'font-family': defaultConditionalStyle.properties['font-family'],
                'font-size': defaultConditionalStyle.properties['font-size'],
                'font-style': defaultConditionalStyle.properties['font-style'],
                'font-weight': defaultConditionalStyle.properties['font-weight'],
                color: range.color ?? defaultConditionalStyle.properties.color,
                'background-color': range['background-color'] ?? defaultConditionalStyle.properties['background-color'],
                icon: range.icon ? range.icon.trim() : defaultConditionalStyle.properties.icon
            }
        } as IPivotTableWidgetConditionalStyle
        formattedWidget.settings.conditionalStyles.enabled = true
        formattedWidget.settings.conditionalStyles.conditions.push(tempConditionalStyle)
    })
}

// export const getTooltipFromColumn = (formattedWidget: IWidget, tempColumn: any) => {
//     if (tempColumn.hasOwnProperty('hideTooltip') || tempColumn.style?.hasOwnProperty('tooltip')) {
//         const tempTooltipStyle = {
//             target: [getColumnId(tempColumn.name)],
//             enabled: !tempColumn.hideTooltip,
//             prefix: tempColumn.style?.tooltip?.prefix ?? '',
//             suffix: tempColumn.style?.tooltip?.suffix ?? '',
//             precision: tempColumn.style?.tooltip?.precision ?? 0,
//             header: {
//                 enabled: tempColumn.style?.enableCustomHeaderTooltip ?? false,
//                 text: tempColumn.style?.customHeaderTooltip ?? ''
//             }
//         }
//         formattedWidget.settings.tooltips.push(tempTooltipStyle)
//     }
// }

// const addVisualisationTypeAttributeColumn = (formattedWidget: IWidget, tempColumn: any) => {
//     formattedWidget.settings.visualization.visualizationTypes.enabled = true
//     formattedWidget.settings.visualization.visualizationTypes.types.push({ target: [getColumnId(tempColumn.name)], type: 'Text', prefix: tempColumn.style?.prefix ?? '', suffix: tempColumn.style?.suffix ?? '', pinned: tempColumn.pinned ?? '' })
// }

// const addVisualisationTypeMeasureColumn = (formattedWidget: IWidget, tempColumn: any) => {
//     formattedWidget.settings.visualization.visualizationTypes.enabled = true
//     const tempVisualizationType = {
//         target: [getColumnId(tempColumn.name)],
//         type: formatColumnVisualizationTypeFromOldModel(tempColumn.visType),
//         precision: tempColumn.precision,
//         prefix: tempColumn.style?.prefix ?? '',
//         suffix: tempColumn.style?.suffix,
//         pinned: tempColumn.pinned ?? ''
//     } as ITableWidgetVisualizationType
//     if ((tempColumn.visType === 'Chart' || tempColumn.visType === 'Text & Chart') && tempColumn.barchart) {
//         tempVisualizationType.min = tempColumn.barchart.minValue ?? 0
//         tempVisualizationType.max = tempColumn.barchart.maxValue ?? 0
//         tempVisualizationType.alignment = tempColumn.barchart.style ? tempColumn.barchart.style['justify-content'] ?? '' : ''
//         tempVisualizationType.color = tempColumn.barchart.style ? hexToRgba(tempColumn.barchart.style.color) : ''
//         tempVisualizationType['background-color'] = tempColumn.barchart.style ? hexToRgba(tempColumn.barchart.style['background-color']) ?? '' : ''
//     }
//     formattedWidget.settings.visualization.visualizationTypes.types.push(tempVisualizationType)
// }

// const formatColumnVisualizationTypeFromOldModel = (visType: string) => {
//     switch (visType) {
//         case 'Text & Chart':
//             return 'Bar'
//         case 'Chart':
//             return 'Bar'
//         case 'Icon only':
//             return 'Icon'
//         default:
//             return 'Text'
//     }
// }

// const getRowConfigurationFromWidgetColumn = (formattedWidget: IWidget, column: any) => {
//     if (column.rowSpan) {
//         formattedWidget.settings.configuration.rows.rowSpan.enabled = true
//         formattedWidget.settings.configuration.rows.rowSpan.column = getColumnId(column.name)
//     }
// }

// const getHeaderConfigurationFromWidgetColumn = (formattedWidget: IWidget, column: any, formattedDashboardModel: IDashboard) => {
//     if (column.style && column.style.hasOwnProperty('hideHeader')) {
//         formattedWidget.settings.configuration.headers.custom.enabled = true
//         formattedWidget.settings.configuration.headers.custom.rules.push({ target: [getColumnId(column.name)], action: 'hide' })
//     }
//     if (column.variables) {
//         formattedWidget.settings.configuration.headers.custom.enabled = true
//         getHeaderConfigurationFromColumnVariable(formattedWidget, column, formattedDashboardModel)
//     }
// }

// const getHeaderConfigurationFromColumnVariable = (formattedWidget: IWidget, column: any, formattedDashboardModel: IDashboard) => {
//     const modelVariables = formattedDashboardModel?.configuration?.variables ?? []
//     column.variables.forEach((variable: { action: string; variable: string; condition: string; value: string }) => {
//         if (variable.action === 'header') {
//             const modelVariable = modelVariables.find((tempVariable: IVariable) => tempVariable.name === variable.variable)
//             const tempHeadersConfigurationRule = { target: [getColumnId(column.name)], action: 'setLabel', compareType: 'variable', variable: variable.variable } as ITableWidgetHeadersRule
//             setHeaderConfigurationRuleValueFromVariable(tempHeadersConfigurationRule, modelVariable, variable)
//             addHeadersRuleToTheModel(tempHeadersConfigurationRule, formattedWidget)
//         }
//     })
// }

// const setHeaderConfigurationRuleValueFromVariable = (rule: ITableWidgetHeadersRule, modelVariable: IVariable | undefined, variable: { action: string; variable: string; condition: string; value: string; key?: string }) => {
//     if (!modelVariable) return
//     switch (modelVariable.type) {
//         case 'static':
//         case 'profile':
//         case 'driver':
//             rule.value = modelVariable.value
//             break
//         case 'dataset':
//             if (modelVariable.column) {
//                 rule.value = modelVariable.value
//             } else {
//                 rule.variableKey = variable.key
//                 rule.variablePivotDatasetOptions = modelVariable.pivotedValues
//                 rule.value = rule.variableKey ? rule.variablePivotDatasetOptions[rule.variableKey] : ''
//             }
//     }
// }

// const addHeadersRuleToTheModel = (rule: ITableWidgetHeadersRule, formattedWidget: IWidget) => {
//     for (let i = 0; i < formattedWidget.settings.configuration.headers.custom.rules.length; i++) {
//         if (formattedWidget.settings.configuration.headers.custom.rules[i].target.includes(rule.target[0])) return
//     }
//     formattedWidget.settings.configuration.headers.custom.rules.push(rule)
// }
