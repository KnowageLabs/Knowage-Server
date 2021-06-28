export interface iBusinessModel {
    id: number,
    name: string,
    dataSourceId: number,
    description: string,
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
    id: number,
    label: string,
    parameter: Object,
    parameterUrlName: string
}