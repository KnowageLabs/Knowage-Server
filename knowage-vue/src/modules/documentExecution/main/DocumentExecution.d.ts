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
    content: string,
    creationDate: string | Date,
    documentId: number,
    id?: number,
    lastChangeDate: string | Date,
    owner: string,
    public: boolean,
    type: string
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
    file: { id: number, name: string, value: string, fileToSave?: { file: {}, fileName: string } }[],
}