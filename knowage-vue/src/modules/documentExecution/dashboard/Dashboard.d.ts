export interface ISheet {
    label: string
    icon?: string
    widgets: Array<object>
}

export interface IWidget {
    id: string
    type: string
    theme: string
    settings: object
    interactions?: Array<object>
}

export interface IInteraction {
    type: string
}

export interface IWidgetPickerType {
    cssClass: string
    descKey: string
    img: string
    name: string
    tags: Array<string>
    type: string
}
