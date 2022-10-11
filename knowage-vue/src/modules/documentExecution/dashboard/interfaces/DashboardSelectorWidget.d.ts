import { IWidgetResponsive, IWidgetTitle, IWidgetPaddingStyle, IWidgetBordersStyle, IWidgetShadowsStyle } from '../Dashboard'

export interface ISelectorWidgetSettings {
    isDateType: boolean,
    sortingOrder: string
    updatable: boolean,
    clickable: boolean,
    configuration: ISelectorWidgetConfiguration,
    style: ISelectorWidgetStyle,
    responsive: IWidgetResponsive,
}

export interface ISelectorWidgetConfiguration {
    selectorType: ISelectorWidgetSelectorType,
    defaultValues: ISelectorWidgetDefaultValues,
    valuesManagement: ISelectorWidgetValuesManagement,
}

export interface ISelectorWidgetSelectorType {
    modality: "singleValue" | "multiValue" | "dropdown" | "multiDropdown" | "datepicker" | "dateRange",
    alignment: "vertical" | "horizontal" | "grid",
    columnSize: string

}

export interface ISelectorWidgetDefaultValues {
    enabled: boolean,
    valueType?: "" | "STATIC" | "FIRST" | "LAST",
    value?: string,
    startDate?: Date | null,
    endDate?: Date | null
}

export interface ISelectorWidgetValuesManagement {
    hideDisabled: boolean,
    enableAll: boolean,
}

export interface ISelectorWidgetStyle {
    title: IWidgetTitle,
    label: ISelectorWidgetLabelStyle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle
}

export interface ISelectorWidgetLabelStyle {
    wrapText: boolean,
    properties: {
        'font-weight': string
        'font-style': string
        'font-size': string
        'font-family': string
        'justify-content': string
        color: string
        'background-color': string
    }
}