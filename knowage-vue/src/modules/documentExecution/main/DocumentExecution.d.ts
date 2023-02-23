import { iParameter } from "@/components/UI/KnParameterSidebar/KnParameterSidebar"

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
    file: { id: number, name: string, value: any, fileToSave?: { file: any, fileName: string } }[],
}

export interface IDocumentNavigationParameter {
    id: string,
    value: IDocumentNavigationParameterValue | string,
    fixed: boolean,
    sourceDriverUrlName?: string,
}

export interface IDocumentNavigationParameterValue {
    label: string,
    type: IDocumentNavigationParameterValueType,
    inputParameterType: any,
    dateFormat: any,
    isInput: boolean
}

export interface IDocumentNavigationParameterValueType {
    valueId: number,
    valueCd: string,
    valueName: string,
    valueDescription: string,
    domainCode: string,
    domainName: string,
    translatedValueDescription: string,
    translatedValueName: string
}

export interface ICrossNavigationParameter {
    targetDriverUrlName: string,
    parameterValue: { value: string | number, description: string }[],
    multivalue: boolean,
    type: 'fixed' | 'fromSourceDocumentDriver' | 'fromSourceDocumentOutputParameter',
    parameterType?: string,
    selectionType?: string,
    outputDriverName?: string
}

export interface ICrossNavigationBreadcrumb {
    document: any,
    label: string,
    crossBreadcrumb?: string,
    filtersData?: { filterStatus: iParameter[], isReadyForExecution: boolean },
    hiddenFormData?: any,
    urlData?: any
}