import { IWidget, ITableWidgetSettings, ITableWidgetPagination, ITableWidgetConditionalStyle, ITableWidgetTooltipStyle, ITableWidgetStyle, IWidgetInteractions, ITableWidgetConfiguration, IWidgetResponsive, ITableWidgetConditionalStyles, IDashboard, IVariable, IDashboardDriver } from '../../Dashboard'
import { getFormattedConfiguration } from './TableWidgetConfigurationHelper'
import { getFormattedStyle } from './TableWidgetStyleHelper'
import { getSettingsFromWidgetColumns } from './TableWidgetColumnSettingsHelper'
import * as tableWidgetDefaultValues from '../../widget/WidgetEditor/helpers/tableWidget/TableWidgetDefaultValues'
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFiltersForColumns } from '../DashboardBackwardCompatibilityHelper'
import { getFormattedInteractions } from '../common/WidgetInteractionsHelper'
import { getFormattedWidgetColumns } from '../common/WidgetColumnHelper'

const columnNameIdMap = {}

export const formatTableWidget = (widget: any, formattedDashboardModel: IDashboard, drivers: IDashboardDriver[]) => {
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId,
        type: widget.type,
        columns: getFormattedWidgetColumns(widget, columnNameIdMap),
        theme: '',
        style: {},
        settings: {} as ITableWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget, formattedDashboardModel, drivers)
    getFiltersForColumns(formattedWidget, widget)
    getSettingsFromWidgetColumns(formattedWidget, widget, formattedDashboardModel, columnNameIdMap)
    return formattedWidget
}

const getFormattedWidgetSettings = (widget: any, formattedDashboardModel: IDashboard, drivers: IDashboardDriver[]) => {
    const formattedSettings = {
        sortingColumn: getColumnId(widget.settings?.sortingColumn) ?? '',
        sortingOrder: widget.settings?.sortingOrder ?? '',
        updatable: widget.updateble,
        clickable: widget.cliccable,
        conditionalStyles: getFormattedConditionalStyles(widget, formattedDashboardModel, drivers),
        configuration: getFormattedConfiguration(widget) as ITableWidgetConfiguration,
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        pagination: getFormattedPaginations(widget),
        style: getFormattedStyle(widget) as ITableWidgetStyle,
        tooltips: tableWidgetDefaultValues.getDefaultTooltips() as ITableWidgetTooltipStyle[],
        visualization: tableWidgetDefaultValues.getDefaultVisualizations(),
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes() as IWidgetResponsive
    } as ITableWidgetSettings
    return formattedSettings
}
const getFormattedConditionalStyles = (widget: any, formattedDashboardModel: IDashboard, drivers: IDashboardDriver[]) => {
    const formattedStyles = { enabled: false, conditions: [] } as ITableWidgetConditionalStyles
    if (widget.settings.rowThresholds?.enabled) {
        widget.settings.rowThresholds.list.forEach((rowThreshold: any) => {
            formattedStyles.conditions.push(createConditionFromRowThreshold(rowThreshold, formattedDashboardModel, drivers))
        })
    }

    return formattedStyles
}

const createConditionFromRowThreshold = (rowThreshold: any, formattedDashboardModel: IDashboard, drivers: IDashboardDriver[]) => {
    const conditionStyle = {
        target: getColumnId(rowThreshold.column),
        applyToWholeRow: false,
        condition: { type: rowThreshold.compareValueType, operator: rowThreshold.condition, value: '' },
        properties: {
            'justify-content': '',
            'font-family': '',
            'font-size': '',
            'font-style': '',
            'font-weight': '',
            color: '',
            'background-color': '',
            icon: ''
        }
    } as ITableWidgetConditionalStyle
    switch (conditionStyle.condition.type) {
        case 'static':
            conditionStyle.condition.value = rowThreshold.compareValue
            break
        case 'parameter':
            conditionStyle.condition.value = getParameterValue(rowThreshold.compareValue, drivers)
            conditionStyle.condition.parameter = rowThreshold.compareValue
            break
        case 'variable':
            conditionStyle.condition.variable = rowThreshold.compareValue
            updateConditionalStyleFromVariable(conditionStyle, rowThreshold, formattedDashboardModel)
    }

    if (rowThreshold.style) {
        delete rowThreshold.style['border-top-color']
        delete rowThreshold.style['border-bottom-color']
        conditionStyle.properties = { ...rowThreshold.style, icon: '' }
    }

    return conditionStyle
}

const updateConditionalStyleFromVariable = (conditionStyle: ITableWidgetConditionalStyle, rowThreshold: any, formattedDashboardModel: IDashboard) => {
    const modelVariable = formattedDashboardModel.configuration.variables?.find((variable: IVariable) => variable.name === rowThreshold.compareValue)
    setConditionalStyleValueFromVariable(conditionStyle, modelVariable, rowThreshold)
}

const setConditionalStyleValueFromVariable = (conditionStyle: ITableWidgetConditionalStyle, modelVariable: IVariable | undefined, rowThreshold: any) => {
    if (!modelVariable) return
    switch (modelVariable.type) {
        case 'static':
        case 'profile':
        case 'driver':
            conditionStyle.condition.value = modelVariable.value
            break;
        case 'dataset':
            if (modelVariable.column) {
                conditionStyle.condition.value = modelVariable.value
            } else {
                conditionStyle.condition.variableKey = rowThreshold.compareValueKey
                conditionStyle.condition.variablePivotDatasetOptions = modelVariable.pivotedValues
                conditionStyle.condition.value = conditionStyle.condition.variableKey ? conditionStyle.condition.variablePivotDatasetOptions[conditionStyle.condition.variableKey] : ''
            }
    }
}

export const getFormattedPaginations = (widget: any) => {
    if (!widget.settings?.pagination) return tableWidgetDefaultValues.getDefaultPagination()
    return {
        enabled: widget.settings.pagination.enabled, properties: { offset: 0, itemsNumber: widget.settings.pagination.itemsNumber ?? 15, totalItems: 0 }
    } as ITableWidgetPagination
}

const getParameterValue = (parameterName: string, drivers: IDashboardDriver[]) => {
    const index = drivers.findIndex((driver: IDashboardDriver) => driver.driverLabel === parameterName)
    return index !== -1 ? drivers[index].value : ''
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}
