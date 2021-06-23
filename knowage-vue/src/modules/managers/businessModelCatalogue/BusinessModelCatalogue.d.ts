export interface iBusinessModel {
    id: number,
    name: string,
    description: string,
    category: number
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