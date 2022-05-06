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
        criterionPriority: []
    },
    status: string,
    targets: iScorecardTarget[],
    groupedKpis: { status: null, count: number }[]
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
    category: {
        valueId: number,
        valueCd: string,
        valueName: string,
        valueDescription: string,
        domainCode: string,
        domainName: string,
        translatedValueName: string,
        translatedValueDescription: string
    },
    threshold: {
        id: number,
        description: string,
        name: string,
        typeId: number,
        type: string,
        thresholdValues: {
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
        }[],
        usedByKpi: boolean
    },
    status: string | null
}

export interface iScorecardTarget {
    id?: number,
    name: string,
    criterion: iScorecardCriterion,
    options: {
        criterionPriority: []
    },
    status: string,
    kpis: iKpi[],
    groupedKpis: { status: null, count: number }[]
}