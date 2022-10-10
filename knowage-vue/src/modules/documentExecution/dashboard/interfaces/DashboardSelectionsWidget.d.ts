import { IWidgetTitle } from '../Dashboard'

export interface ISelectionsWidgetSettings {
    sortingOrder?: string
    updatable: boolean,
    clickable: boolean,
    type: "chips" | "list",
    valuesManagement: ISelectionsWidgetValuesManagement,
    noSelections: ISelectionsWidgetNoSelections,
    style: ISelectionWidgetStyle
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
    chips: ISelectionWidgetChips,
    rows: ISelectionWidgetRows,
    background: ISelectionWidgetBackground
}

export interface ISelectionWidgetChips {
    height: string,
    properties: {
        color: string
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

export interface ISelectionWidgetBackground {
    properties: {
        color: string
    }
}