export interface ISheet {
    label: string
    icon?: string
    widgets: Array<object>
}

export interface IWidget {
    id?: string
    dataset: number
    type: string
    columns: IWidgetColumn[]
    conditionalStyles: any[]
    interactions: any[]
    theme: string
    styles: any
    settings: any
    temp?: any
    functions?: any
    new?: boolean
}

export interface IWidgetColumn {
    dataset?: number // za brisanje
    columnName: string  // model
    name?: string  // za brisanje
    alias: string  // model
    type: string  // model
    fieldType: string  // model
    multiValue: boolean,  // model
    aggregation?: string,  // model
    style?: any,  // ??? 
    enableTooltip?: boolean, // ???
    visType?: string   // ???
    filter?: IWidgetColumnFilter
}

export interface IWidgetColumnFilter {
    enabled: boolean
    operator: string
    value: string
}

export interface IInteraction {
    type: string
}

export interface IWidgetEditorDataset {
    id: number
    label: string
    cache: boolean
    parameters: any[]
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
        measures: any[]
        categories: IDatasetOptionCategory[]
        dataset: string
    }
    parameters: any
    selections: any
    indexes: any[]
}

export interface IDatasetOptionCategory {
    id: string
    alias: string
    columnName: string
    orderType: string
    funct: string
}

export interface IDatasetColumn {
    dataset?: number
    name: string
    alias: string
    type: string
    properties: any
    fieldType: string
    multiValue: boolean
    precision: number
    scale: number
    personal: boolean
    decript: boolean
    subjectId: boolean
}

export interface IDataset {
    id: {
        dsId: number
        versionNum: number
        organization: string
    }
    name: string
    description: string
    label: string
    active: boolean
    type: string
    configuration: any
    pivotColumnName: string
    pivotRowName: string
    pivotColumnValue: string
    numRows: boolean
    persisted: boolean
    persistedHDFS: boolean
    persistTableName: string
    owner: string
    userIn: any
    userUp: any
    userDe: any
    sbiVersionIn: any
    sbiVersionUp: any
    sbiVersionDe: any
    metaVersion: any
    timeIn: any
    timeUp: any
    timeDe: any
    scope: any
    federation: any
    tags: any[]
    scopeId: number
    transformerId: number
    metadata: {
        fieldsMeta: any[]
        properties: any
    }
    categoryId: number
    parameters: IDatasetParameters[]
    isRealtime: boolean
    isCachingSupported: boolean
    isIterable: boolean
    isNearRealtimeSupported: boolean,
    cache?: boolean,
    indexes?: any[],
    drivers?: any[]
}

interface IDatasetParameters {
    name: string
    type: string
    defaultValue: string
    multiValue: boolean
}

interface IAssociation {
    id: string
    fields: IAssociationField[]
    validation?: {
        isValid: boolean
        msg: string
    }
}

interface IAssociationField {
    column: string
    dataset: number
}

export interface IModelDataset {
    id: number
    cache: boolean
    indexes: string[]
    parameters: IModelDatasetParameter[],
    drivers: any[]
}

interface IModelDatasetParameter {
    multivalue: boolean
    name: string
    type: string
    value: string
}

export interface IIcon {
    id: number
    name: string
    value: string
}
