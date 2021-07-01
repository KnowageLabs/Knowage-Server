export interface iBusinessModel {
    id: number,
    name: string,
    dataSourceId: number,
    description: string,
    dataSourceLabel: string,
    category: any,
    modelLocked: boolean,
    smartView: boolean,
    tablePrefixLike?: string,
    tablePrefixNotLike?: string
}

export interface iBusinessModelVersion {
    id: number,
    fileName: string,
    creationDate: number,
    active: Boolean,
    hasContent: Boolean,
    hasLog: Boolean,
    hasFileModel: Boolean,
}

export interface iBusinessModelDriver {
    id?: number,
    biMetaModelID: number,
    label?: string,
    parameter?: { id: number, label: string, name: string },
    parameterUrlName?: string,
    priority: number,
    multivalue: Boolean,
    modifiable: number,
    required: Boolean,
    visible: Boolean,
    numberOfErrors?: number,
    status?: string
}