import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard"
import { IPivotTableSettings } from "@/modules/documentExecution/dashboard/interfaces/pivotTable/DashboardPivotTableWidget"
import { removeColumnFromSubmodel } from "../tableWidget/TableWidgetFunctions"
import * as pivotTableDefaultValues from './PivotTableDefaultValues'
import * as widgetCommonDefaultValues from '../common/WidgetCommonDefaultValues'

export const createNewPivotTableWidgetSettings = () => {
    return {
        updatable: true,
        clickable: true,
        conditionalStyles: pivotTableDefaultValues.getDefaultConditionalStyles(),
        visualization: pivotTableDefaultValues.getDefaultVisualisationSettings(),
        configuration: {
            columns: pivotTableDefaultValues.getDefaultTotalValues(),
            rows: pivotTableDefaultValues.getDefaultTotalValues(),
            fieldPicker: pivotTableDefaultValues.getDefaultFieldPicker(),
            fieldPanel: pivotTableDefaultValues.getDefaultFieldPanel(),
            exports: { showExcelExport: true, showScreenshot: true },
        },
        interactions: {
            crossNavigation: widgetCommonDefaultValues.getDefaultCrossNavigation(),
            selection: { enabled: false }
        },
        style: {
            title: widgetCommonDefaultValues.getDefaultTitleStyle(),
            fields: pivotTableDefaultValues.getDefaultColumnStyles(),
            fieldHeaders: pivotTableDefaultValues.getDefaultColumnStyles(),
            background: widgetCommonDefaultValues.getDefaultBackgroundStyle(),
            borders: widgetCommonDefaultValues.getDefaultBordersStyle(),
            padding: widgetCommonDefaultValues.getDefaultPaddingStyle(),
            shadows: widgetCommonDefaultValues.getDefaultShadowsStyle(),
            totals: pivotTableDefaultValues.getDefaultTotals(),
            subTotals: pivotTableDefaultValues.getDefaultTotals(),
            columnHeaders: pivotTableDefaultValues.getDefaultColumnHeadersStyle(),
            rowHeaders: pivotTableDefaultValues.getDefaultColumnHeadersStyle()
        },
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes(),
        tooltips: pivotTableDefaultValues.getDefaultTooltips()
    } as IPivotTableSettings
}

export const removeColumnFromPivotTableWidgetModel = (widgetModel: IWidget, column: IWidgetColumn) => {
    removeColumnFromConditionalStyles(widgetModel, column)
    removeColumnFromSubmodel(column, widgetModel.settings.style.fields.styles, 'target', 'columnRemovedFromColumnStyle', true)
    removeColumnFromSubmodel(column, widgetModel.settings.style.fieldHeaders.styles, 'target', 'columnRemovedFromColumnStyle', true)
    removeColumnFromSubmodel(column, widgetModel.settings.visualization.visualizationTypes.types, 'target', 'columnRemovedFromVisualizationTypes', true)
    removeColumnFromSubmodel(column, widgetModel.settings.tooltips, 'target', 'columnRemovedFromTooltips', true)
}

const removeColumnFromConditionalStyles = (widgetModel: IWidget, column: IWidgetColumn) => {
    const conditionalStyles = widgetModel.settings.conditionalStyles.conditions
    for (let i = conditionalStyles.length - 1; i >= 0; i--) {
        if (conditionalStyles[i].target === column.id) conditionalStyles.splice(i, 1)
    }
}