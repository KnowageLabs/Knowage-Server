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

export interface IWidgetEditorDataset {
    id: number,
    label: string,
    cache: boolean,
    parameters: any[],
}