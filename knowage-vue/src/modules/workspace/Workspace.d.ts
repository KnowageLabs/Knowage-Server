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
