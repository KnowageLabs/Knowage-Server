import { IWidgetBackgroundStyle, IWidgetResponsive, IWidgetRowsStyle, IWidgetTitle } from '../Dashboard'

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
    exports: IWidgetExports
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
    rows: IWidgetRowsStyle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}


export interface ISelectionWidgetChipsStyle {
    height: number,
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