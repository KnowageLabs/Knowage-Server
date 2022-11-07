

export interface ITextWidgetSettings {
    sortingColumn?: string,
    sortingOrder?: string,
    updatable: boolean,
    clickable: boolean,
    configuration: ITextWidgetConfiguration,
    style: ITextWidgetStyle,
    responsive: IWidgetResponsive
}

export interface ITextWidgetConfiguration {
    content: ITextWidgetContent
}

export interface ITextWidgetContent {
    text: string
}

export interface ITextWidgetStyle {
    title: IWidgetTitle,
    padding: IWidgetPaddingStyle,
    borders: IWidgetBordersStyle,
    shadows: IWidgetShadowsStyle,
    background: IWidgetBackgroundStyle
}