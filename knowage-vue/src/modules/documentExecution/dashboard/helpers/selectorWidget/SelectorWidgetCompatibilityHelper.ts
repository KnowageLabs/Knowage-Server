import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard"
import { ISelectorWidgetDefaultValues, ISelectorWidgetSelectorType, ISelectorWidgetSettings } from "@/modules/documentExecution/dashboard/interfaces/DashboardSelectorWidget"
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
        selectorType: getFormattedSelectorType(widget),
        defaultValues: getFormattedDefaultValues(widget),
        valuesManagement: getFormattedWidgetValuesManagement(widget),
        style: {} as any,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as ISelectorWidgetSettings
    return formattedSettings
}

const getFormattedSelectorType = (widget: any) => {
    if (!widget.content || widget.content.settings) return selectorWidgetDefaultValues.getDefaultSelectorType()

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
    if (!widget.content || widget.content.settings) return selectorWidgetDefaultValues.getDefaultValues()
    const formattedDefaultValues = {
        enabled: false,
        valueType: widget.content.settings.defaultValue,
    } as ISelectorWidgetDefaultValues
    if (formattedDefaultValues.valueType) formattedDefaultValues.enabled = true
    if (formattedDefaultValues.valueType === 'STATIC') formattedDefaultValues.value = widget.content.settings.staticValues
    return formattedDefaultValues
}


const getFormattedWidgetValuesManagement = (widget: any) => {
    return {} as any
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}

