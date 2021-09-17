export interface iLov {
    id: number,
    name: string,
    description: string,
    label: string,
    lovProvider: any,
    itypeId: string,
    itypeCd: string,
    lovProviderJSON: any
}

export interface iDomain {
    VALUE_NM: string,
    VALUE_DS: string,
    VALUE_ID: number,
    VALUE_CD: string
}

export interface iDatasource {
    dsId: number,
    descr: string,
    label: string,
}

export interface iProfileAttribute {
    attributeId: number,
    attributeName: string,
    attributeDescription: string,
}

export interface iFixedValue {
    VALUE: string,
    DESCRIPTION: string
}