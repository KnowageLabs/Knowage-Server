export interface IDocument {
    biObjId: number
    functId: number
    objId: number
    subObjId: number
    requestTime: number
    subObjName?: string
    documentDescription?: string
    documentLabel?: string
    documentName?: string
    documentPath?: string
    documentType?: string
    previewFile?: string
    engineName?: any
}

export interface IFolder {
    id?: string
    code: string
    descr?: string
    name: string
    path: string
    userIn: string
    functId: number
    parentFunct?: number
    prog: number
    timeIn: number
    data?: { name: string; hasDocuments: boolean }
    items?: IFolder[]
}

export interface IPackage {
    jobName: string
    jobGroup: string
    jobDescription: string
    jobClass: string
    jobDurability: boolean
    jobRequestRecovery: boolean
    jobMergeAllSnapshots: boolean
    jobCollateSnapshots: boolean
    useVolatility: boolean
    jobParameters: { name: string; value: string }[]
    documents: any[]
    triggers: iTrigger[]
    edit?: boolean
    numberOfDocuments?: number
}

export interface ITrigger {
    jobName: string
    jobGroup: string
    triggerName: string
    triggerGroup: string
}

export interface ISchedulation {
    id: number
    biobjId: number
    name: string
    description: string
    dateCreation: number
    time: string
    binId: any
    content: any
    contentType: any
    schedulation: any
    scheduler: any
    schedulationStartDate: any
    sequence: any
    urlPath?: string
}

export interface IBusinessModel {
    id: number
    name: string
    description: string
    category: number
    dataSourceLabel: string
    dataSourceId: number
    modelLocked: boolean
    modelLocker: any
    smartView: boolean
    tablePrefixLike: any
    tablePrefixNotLike: any
    drivers: any[]
    metamodelDrivers: any
}

export interface IFederatedDataset {
    federation_id?: number
    name: string
    label: string
    description: string
    relationships: any
    degenerated?: boolean
    owner?: any
}

export interface INode {
    key: number | string
    icon?: string
    id: number
    parentId?: number
    label: string
    children?: iNode[]
    selectable?: Boolean
    data: string | any
    customIcon?: string
}

export interface IDataset {
    actions: any[]
    usedByNDocs: number
    drivers: any[]
    meta: IDatasetMetadata
    dateIn: string
    description: string
    tags: string[]
    author: string
    name: string
    id: number
    owner: string
    label: string
    catTypeId: number
    catTypeCd: string
    pars: any[]
}

export interface IDatasetMetadata {
    dataset: any[]
    columns: any[]
}
