export interface iTargetDefinition {
    id?: number | null
    name?: string
    startValidity?: Date
    endValidity?: Date
    author?: string
    values?: iValues[]
    category?: iCategory
}

export interface iCategory {
    valueId?: number | null
    valueCd?: string
    valueName?: string
    valueDescription?: string
    domainCode?: string
    domainName?: string
    translatedValueName?: string
    translatedValueDescription?: string
}

export interface iValues {
    kpiId: number | null
    kpiVersion?: number
    kpiName?: string
    kpiCategory?: string
    kpiDate?: Date
    kpiAuthor?: string
    targetId?: number | null
    value?: number
}
