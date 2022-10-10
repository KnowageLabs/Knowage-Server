import { IWidgetResponsive, IWidgetTitle, IWidgetPaddingStyle, IWidgetBordersStyle } from '../Dashboard'

export interface ISelectorWidgetSettings {
    isDateType: boolean,
    sortingOrder: string
    updatable: boolean,
    clickable: boolean,
    selectorType: ISelectorWidgetSelectorType,
    defaultValues: ISelectorWidgetDefaultValues,
    valuesManagement: ISelectorWidgetValuesManagement,
    style: ISelectorWidgetStyle,
    responsive: IWidgetResponsive,
}

export interface ISelectorWidgetSelectorType {
    modality: "singleValue" | "multiValue" | "dropdown" | "multiDropdown" | "datepicker" | "dateRange",
    alignment: "vertical" | "horizontal" | "grid",
    columnSize: string

}

export interface ISelectorWidgetDefaultValues {
    enabled: boolean,
    valueType: "static" | "first" | "last",
    value: string
}

export interface ISelectorWidgetValuesManagement {
    hideDisabled: boolean,
    enableAll: boolean,
}

export interface ISelectorWidgetStyle {
    title: IWidgetTitle,
    label: ISelectorWidgetLabelStyle,
    background: ISelectorWidgetBackgroundStyle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle
}

export interface ISelectorWidgetLabelStyle {
    wrapText: boolean,
    properties: {
        color: string
    }
}

export interface ISelectorWidgetBackgroundStyle {
    properties: {
        color: string
    }
}