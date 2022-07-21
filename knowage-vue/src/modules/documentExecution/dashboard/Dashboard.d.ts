export interface ISheet {
    label: string
    icon?: string
    widgets: Array<object>
}

export interface IWidget {
    id?: string,
    type: string,
    columns: IWidgetColumn[],
    conditionalStyles: any[],
    datasets: any[],
    interactions: any[],
    theme: string,
    styles: any,
    settings: any
}

export interface IWidgetColumn {
    dataset: number,
    name: string,
    alias: string,
    type: string,
    fieldType: string,
    aggregation: string,
    style: any
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

interface IDatasetColumn {
    name: string,
    alias: string,
    type: string,
    properties: any,
    fieldType: string,
    multiValue: boolean,
    precision: number,
    scale: number,
    personal: boolean,
    decript: boolean,
    subjectId: boolean
}