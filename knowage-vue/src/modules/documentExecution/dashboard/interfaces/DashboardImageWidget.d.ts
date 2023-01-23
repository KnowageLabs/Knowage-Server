import { IWidgetExports, IWidgetInteractions, IWidgetResponsive } from "../Dashboard";

export interface IImageWidgetSettings {
    updatable: boolean,
    clickable: boolean,
    configuration: IImageWidgetConfiguration
    style: IImageWidgetStyle,
    interactions: IWidgetInteractions,
    responsive: IWidgetResponsive
}


export interface IImageWidgetConfiguration {
    image: IImageWidgetImageSettings,
    exports: IWidgetExports
}
export interface IImageWidgetImageSettings {
    id: number,
    style: {
        height: string,
        width: string,
        "background-position-x": "left" | "center" | "right",
        "background-position-y": "top" | "center" | "bottom"
    },
}

export interface IImageWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}

export interface IImage {
    imgId: number,
    lastmod: string,
    name: string,
    size: number,
    url: string,
    urlPreview: string
}