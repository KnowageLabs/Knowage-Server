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
export interface IWidgetPickerType {
    cssClass: string
    descKey: string
    img: string
    name: string
    tags: Array<string>
    type: string
}

export interface IDatasetOptions {
    aggregations: {
        measures: any[],
        categories: IDatasetOptionCategory[],
        dataset: string
    },
    parameters: any,
    selections: any,
    indexes: any[]
}

interface IDatasetOptionCategory {
    id: string,
    alias: string,
    columnName: string,
    orderType: string,
    funct: string
}