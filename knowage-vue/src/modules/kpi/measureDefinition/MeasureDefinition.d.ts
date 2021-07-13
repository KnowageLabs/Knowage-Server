export interface iMeasure {
    id: number,
    alias: string,
    aliasId: number,
    rule: string,
    ruleId: number,
    ruleVersion: number,
    category: iDomain,
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

export interface iRule {
    id?: number,
    version: number,
    name: string,
    definition: string,
    dataSourceId: number,
    dataSource?: iDatasource,
    ruleOutputs: iMeasure[],
    placeholders: iPlaceholder[]
}

export interface iPlaceholder {
    id?: number,
    name: string,
    ruleId?: number | null,
    value: string | null
}

export interface iDatasource {
    DATASOURCE_ID: number,
    DATASOURCE_LABEL: label,
}