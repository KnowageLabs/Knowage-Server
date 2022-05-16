export interface iScorecard {
    id?: number,
    name: string,
    creationDate?: number | Date,
    author?: string,
    perspectives: iPerspective[],
    description?: string
}

export interface iPerspective {
    id?: number,
    name: string,
    criterion: iScorecardCriterion,
    options: {
        criterionPriority: any[]
    },
    status: string,
    targets: iScorecardTarget[],
    groupedKpis?: { status: null, count: number }[]
    groupedTargets?: any[],
    statusColor?: string | null,
    updated?: boolean
}

export interface iScorecardCriterion {
    valueId: number,
    valueCd: string,
    valueName: string,
    valueDescription: string,
    domainCode: string,
    domainName: string,
    translatedValueName: string,
    translatedValueDescription: string
}

export interface iKpi {
    id: number,
    version: number,
    name: string,
    author: string,
    dateCreation: number | Date,
    active: boolean,
    enableVersioning: boolean,
    definition: string,
    cardinality: string,
    placeholder: string,
    category: iCategory,
    threshold: iThreshold,
    status: string | null
}

export interface iScorecardTarget {
    id?: number,
    name: string,
    criterion: iScorecardCriterion,
    options: {
        criterionPriority: any[]
    },
    status: string,
    kpis: iKpi[],
    groupedKpis?: { status: null, count: number }[],
    statusColor?: string | null,
    updated?: boolean
}

export interface iCategory {
    valueId: number,
    valueCd: string,
    valueName: string,
    valueDescription: string,
    domainCode: string,
    domainName: string,
    translatedValueName: string,
    translatedValueDescription: string
}

export interface iThreshold {
    id: number,
    description: string,
    name: string,
    typeId: number,
    type: string,
    thresholdValues: iThresholdValues[],
    usedByKpi: boolean
}

export interface iThresholdValues {
    id: number,
    position: number,
    label: string,
    color: string,
    severityId: number,
    severityCd: string,
    minValue: number,
    includeMin: boolean,
    maxValue: number,
    includeMax: boolean
}