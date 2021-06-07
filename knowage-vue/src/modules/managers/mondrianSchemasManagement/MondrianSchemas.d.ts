export interface iSchema {
    id?: string
    name: string
    description?: string,
    type: string
    currentContentId: number
}

export interface iVersion {
    id?: string
    fileName: string
    creationDate?: string,
    creationUser: string,
    active: string
}