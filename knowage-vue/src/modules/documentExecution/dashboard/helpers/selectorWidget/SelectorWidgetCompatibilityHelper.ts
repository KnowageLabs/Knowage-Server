import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard"
import { ISelectorWidgetDefaultValues, ISelectorWidgetSelectorType, ISelectorWidgetSettings, ISelectorWidgetValuesManagement, ISelectorWidgetConfiguration } from "@/modules/documentExecution/dashboard/interfaces/DashboardSelectorWidget"
import { getFormattedStyle } from './SelectorWidgetStyleHelper'
import cryptoRandomString from 'crypto-random-string'
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as selectorWidgetDefaultValues from '../../widget/WidgetEditor/helpers/selectorWidget/SelectorWidgetDefaultValues'


const columnNameIdMap = {}

export const formatSelectorWidget = (widget: any) => {
    console.log('SelectorWidgetCompatibilityHelper - formatSelectorWidget called for: ', widget)
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId,
        type: widget.type,
        columns: getFormattedSelectionColumn(widget),
        theme: '',
        settings: {} as ISelectorWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget) as ISelectorWidgetSettings

    console.log('SelectorWidgetCompatibilityHelper - FORMATTED WIDGET: ', formattedWidget)
    return formattedWidget
}

const getFormattedSelectionColumn = (widget: any) => {
    const formattedColumns = [] as IWidgetColumn[]
    if (widget.content && widget.content.selectedColumn) {
        const formattedColumn = { id: cryptoRandomString({ length: 16, type: 'base64' }), columnName: widget.content.selectedColumn.name, alias: widget.content.selectedColumn.alias, type: widget.content.selectedColumn.type, fieldType: widget.content.selectedColumn.fieldType, multiValue: widget.content.selectedColumn.multiValue, filter: {} } as IWidgetColumn
        formattedColumns.push(formattedColumn)
        columnNameIdMap[formattedColumn.columnName] = formattedColumn.id
    }
    return formattedColumns

}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        isDateType: widget.content.selectedColumn && (widget.content.selectedColumn.type === 'oracle.sql.TIMESTAMP' || widget.content.selectedColumn.type === 'java.util.Date'),
        sortingOrder: widget.settings?.sortingOrder ?? '',
        updatable: widget.updateble,
        clickable: widget.cliccable,
        configuration: getFormattedConfiguration(widget),
        style: getFormattedStyle(widget),
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as ISelectorWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return {
        selectorType: getFormattedSelectorType(widget),
        defaultValues: getFormattedDefaultValues(widget),
        valuesManagement: getFormattedWidgetValuesManagement(widget),
    } as ISelectorWidgetConfiguration
}

const getFormattedSelectorType = (widget: any) => {
    if (!widget.settings) return selectorWidgetDefaultValues.getDefaultSelectorType()

    const formattedSelectorType = {
        modality: widget.settings.modalityValue ?? 'radio',
        alignment: widget.settings.modalityView ?? 'vertical',
        columnSize: widget.settings.gridColumnsWidth ?? ''
    } as ISelectorWidgetSelectorType
    if (widget.content.selectedColumn?.type === 'oracle.sql.TIMESTAMP') {
        formattedSelectorType.modality = formattedSelectorType.modality === 'singleValue' ? 'datepicker' : 'dateRange'
    }

    return formattedSelectorType
}

const getFormattedDefaultValues = (widget: any) => {
    if (!widget.settings) return selectorWidgetDefaultValues.getDefaultValues()
    const formattedDefaultValues = {
        enabled: false,
    } as ISelectorWidgetDefaultValues
    if (widget.settings.defaultValue) formattedDefaultValues.valueType = widget.settings.defaultValue
    if (widget.settings.staticValues) formattedDefaultValues.value = widget.settings.staticValues
    if (widget.settings.defaultStartDate) formattedDefaultValues.startDate = new Date(widget.settings.defaultStartDate)
    if (widget.settings.defaultEndDate) formattedDefaultValues.endDate = new Date(widget.settings.defaultEndDate)
    if (formattedDefaultValues.valueType || widget.settings.defaultStartDate || widget.settings.defaultEndDate) formattedDefaultValues.enabled = true
    return formattedDefaultValues
}


const getFormattedWidgetValuesManagement = (widget: any) => {
    if (!widget.settings) return selectorWidgetDefaultValues.getDefaultValuesManagement()
    return { hideDisabled: widget.settings.hideDisabled ?? false, enableAll: widget.settings.enableAll ?? false } as ISelectorWidgetValuesManagement
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}

