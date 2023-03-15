import { IWidget } from '../../Dashboard'
import { IPivotTableWidgetConditionalStyle, IPivotTableWidgetVisualizationType } from '../../interfaces/pivotTable/DashboardPivotTableWidget'
import * as pivotTableDefaultValues from '../../widget/WidgetEditor/helpers/pivotTableWidget/PivotTableDefaultValues'
import { getColumnId } from './PivotTableCompatibilityHelper'

export const getSettingsFromPivotTableWidgetColumns = (formattedWidget: IWidget, widget: any) => {
    getSettingsFromMeasureColumns(formattedWidget, widget)

}

const getSettingsFromMeasureColumns = (formattedWidget: IWidget, widget: any) => {
    for (let i = 0; i < widget.content.crosstabDefinition.measures.length; i++) {
        const tempColumn = widget.content.crosstabDefinition.measures[i]
        console.log('----------------- TEMP COLUMN: ', tempColumn)
        getVisualizationTypeConfigurationsFromColumn(formattedWidget, tempColumn)
        getConditionalStyleFromColumn(formattedWidget, tempColumn)
    }
}


const addVisualisationTypeMeasureColumn = (formattedWidget: IWidget, tempColumn: any) => {
    formattedWidget.settings.visualization.visualizationTypes.enabled = true
    const tempVisualizationType = {
        target: [getColumnId(tempColumn.id)],
        prefix: tempColumn.style?.prefix ?? '',
        suffix: tempColumn.style?.suffix,
        type: tempColumn.visType ?? 'Text',
        precision: tempColumn.style?.precision !== undefined ? tempColumn.style.precision : 2
    } as IPivotTableWidgetVisualizationType

    formattedWidget.settings.visualization.visualizationTypes.types.push(tempVisualizationType)
}

const getVisualizationTypeConfigurationsFromColumn = (formattedWidget: IWidget, tempColumn: any) => {
    addVisualisationTypeMeasureColumn(formattedWidget, tempColumn)
}

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
