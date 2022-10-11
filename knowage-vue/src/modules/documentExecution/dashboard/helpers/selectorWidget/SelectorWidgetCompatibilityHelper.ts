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
        isDateType: widget.content.selectedColumn?.type === 'oracle.sql.TIMESTAMP',
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
        valueType: widget.settings.defaultValue,
    } as ISelectorWidgetDefaultValues
    if (formattedDefaultValues.valueType) formattedDefaultValues.enabled = true
    if (formattedDefaultValues.valueType === 'STATIC') formattedDefaultValues.value = widget.settings.staticValues
    return formattedDefaultValues
}


const getFormattedWidgetValuesManagement = (widget: any) => {
    if (!widget.settings) return selectorWidgetDefaultValues.getDefaultValuesManagement()
    return { hideDisabled: widget.settings.hideDisabled ?? false, enableAll: widget.settings.enableAll ?? false } as ISelectorWidgetValuesManagement
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}

