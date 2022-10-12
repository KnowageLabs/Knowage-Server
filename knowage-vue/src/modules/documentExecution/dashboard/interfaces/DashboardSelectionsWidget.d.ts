import { IWidgetBackgroundStyle, IWidgetResponsive, IWidgetTitle } from '../Dashboard'

export interface ISelectionsWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    configuration: ISelectionWidgetConfiguration,
    style: ISelectionWidgetStyle,
    responsive: IWidgetResponsive
}

export interface ISelectionWidgetConfiguration {
    type: "chips" | "list",
    valuesManagement: ISelectionsWidgetValuesManagement,
    noSelections: ISelectionsWidgetNoSelections,
}

export interface ISelectionsWidgetValuesManagement {
    showColumn: boolean,
    showDataset: boolean
}

export interface ISelectionsWidgetNoSelections {
    enabled: boolean,
    customText: string
}

export interface ISelectionWidgetStyle {
    title: IWidgetTitle,
    chips: ISelectionWidgetChipsStyle,
    rows: ISelectionWidgetRows,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}


export interface ISelectionWidgetChipsStyle {
    height: string,
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

export interface ISelectionWidgetRows {
    height: string,
    alternatedRows: {
        enabled: string,
        evenBackgroundColor: string,
        oddBackgroundColor: string
    }
}