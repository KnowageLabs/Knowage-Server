export interface iTargetDefinition {
    id: number | null
    name: string
    startValidity: Date
    endValidity: Date
    author: string
    values?: Array
    category: iCategory
}

export interface iCategory {
    valueId: number | null
    valueCd?: string
    valueName?: string
    valueDescription?: string
    domainCode?: string
    domainName?: string
    translatedValueName?: string
    translatedValueDescription?: string
}

export interface iKpi {
    kpiId: number | null
    kpiName: string
    kpiVersion: number
    targetId: number | null
    value: number
}
