export interface ISelectionsWidgetSettings {
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
    title: ISelectionWidgetTitle,
    chips: ISelectionWidgetChips,
    rows: ISelectionWidgetRows,
    background: ISelectionWidgetBackground
}

export interface ISelectionWidgetTitle {
    enabled: boolean,
    text: string,
    height: string,
    properties: {
        color: string
    }
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