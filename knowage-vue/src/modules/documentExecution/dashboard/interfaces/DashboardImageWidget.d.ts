import { IWidgetExports, IWidgetInteractions, IWidgetResponsive } from "../Dashboard";

export interface IImageWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    configuration: IImageWidgetConfiguration
    style: IImageWidgetStyle,
    interactions: IWidgetInteractions,
    responsive: IWidgetResponsive
}

export interface IImageWidgetImageSettings {
    id: number,
    style: {
        height: number,
        width: number,
        "background-position-x": "left" | "center" | "right",
        "background-position-y": "top" | "center" | "bottom"
    },
}

export interface IImageWidgetConfiguration {
    image: IImageWidgetImageSettings,
    exports: IWidgetExports
}

export interface IImageWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}

