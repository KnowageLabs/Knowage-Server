export interface iURLData {
    engineLabel: string,
    sbiExecutionId: string,
    typeCode: string,
    url: string
}

export interface iExporter {
    "name": string,
    "engineType": string,
    "engineDriver": string
}

export interface iSchedulation {
    id: number,
    biobjId: number,
    name: string,
    description: string,
    dateCreation: number | Date
}

export interface iNote {
    owner: string,
    creationDate: string | Date,
    lastChangeDate: string | Date,
    nota: string,
    profile: string,
    type?: string,
    content?: string,
    execReq?: string
}

export interface iMail {
    TO: string,
    CC: string,
    OBJECT: string,
    MESSAGE: string,
    LOGIN: string,
    PASSWORD: string,
    REPLAYTO: string,
}

export interface iMetadata {
    generalMetadata: { name: string, value: string }[],
    shortText: { id: number, name: string, value: string }[],
    longText: { id: number, name: string, value: string }[],
    file: { id: number, name: string, value: string }[]
}