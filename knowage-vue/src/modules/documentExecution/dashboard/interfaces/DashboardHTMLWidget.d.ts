import { IWidgetInteractions, IWidgetResponsive } from "../Dashboard";

export interface IHTMLWidgetSettings {
    sortingColumn?: string,
    sortingOrder?: string,
    updatable: boolean,
    clickable: boolean,
    editor: IHTMLWidgetEditor,
    configuration: IHTMLWidgetConfiguration,
    interactions: IWidgetInteractions,
    style: IHTMLWidgetStyle,
    responsive: IWidgetResponsive
}

export interface IHTMLWidgetEditor {
    css: string,
    html: string
}

export interface IHTMLWidgetConfiguration {
    exports: IWidgetExports
}

export interface IHTMLWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}

