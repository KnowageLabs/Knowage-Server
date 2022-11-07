import { IWidgetResponsive } from "../Dashboard";

export interface IHTMLWidgetSettings {
    sortingColumn?: string,
    sortingOrder?: string,
    updatable: boolean,
    clickable: boolean,
    configuration: IHTMLWidgetConfiguration,
    style: IHTMLWidgetStyle,
    responsive: IWidgetResponsive
}

export interface IHTMLWidgetConfiguration {
    cssToRender: string,
    htmlToRender: string,
    exports: IWidgetExports
}

export interface IHTMLWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}

