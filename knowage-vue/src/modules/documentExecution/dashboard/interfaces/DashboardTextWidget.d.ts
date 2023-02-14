import { IWidgetExports } from "../Dashboard"


export interface ITextWidgetSettings {
    sortingColumn?: string,
    sortingOrder?: string,
    updatable: boolean,
    clickable: boolean,
    editor: ITextWidgetEditor,
    configuration: ITextWidgetConfiguration,
    interactions: IWidgetInteractions,
    style: ITextWidgetStyle,
    responsive: IWidgetResponsive
}

export interface ITextWidgetEditor {
    text: string
}

export interface ITextWidgetConfiguration {
    exports: IWidgetExports
}

export interface ITextWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}