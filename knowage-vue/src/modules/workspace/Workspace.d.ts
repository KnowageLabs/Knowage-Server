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
    jobName: string,
    jobGroup: string,
    jobDescription: string,
    jobClass: string,
    jobDurability: boolean,
    jobRequestRecovery: boolean,
    jobMergeAllSnapshots: boolean,
    jobCollateSnapshots: boolean,
    useVolatility: boolean,
    jobParameters: { name: string, value: string }[],
    documents: any[],
    triggers: iTrigger[],
    edit?: boolean
    numberOfDocuments?: number
}

export interface ITrigger { 
    jobName: string,
    jobGroup: string,
    triggerName: string,
    triggerGroup: string
}

export interface ISchedulation {
    id: number,
    biobjId: number,
    name: string,
    description: string,
    dateCreation: number,
    time: string,
    binId: any,
    content: any,
    contentType: any,
    schedulation: any,
    scheduler: any,
    schedulationStartDate: any,
    sequence: nullany
}