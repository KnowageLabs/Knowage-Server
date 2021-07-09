export interface iMeasure {
    id: number,
    alias: string,
    aliasId: number,
    rule: string,
    ruleId: number,
    ruleVersion: number,
    category: string,
    hierarchy: string,
    type: string,
    author: string,
    dateCreation: number
}

export interface iDomain {
    valueId: number,
    valueCd: string,
    valueName: string,
    valueDescription: string,
    domainCode: string,
    domainName: string
}