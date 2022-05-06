export interface iKpi {
    id: number,
    version: number,
    name: string,
    author: string,
    dateCreation: number | Date,
    active: boolean,
    enableVersioning: boolean,
    definition: null,
    cardinality: null,
    placeholder: null,
    category: iKpiCategory,
    threshold: null
}

export interface iKpiCategory {
    valueId: number,
    valueCd: string,
    valueName: string,
    valueDescription: string,
    domainCode: string,
    domainName: string,
    translatedValueDescription: string,
    translatedValueName: string
}

export interface iScorecard {
    id: number,
    name: string,
    creationDate: number | Date,
    author: string,
    perspectives: any[]
}

export interface iKpiDesigner {
    id?: number
    chart: iChart
}

export interface iChart {
    model: string,
    type: string,
    data: {
        kpi?: iKpiListItem[],
        scorecard?: {
            name: string
        }
    },
    options: iOptions,
    style: iStyle
}


export interface iKpiListItem {
    isSuffix: string,
    name: string,
    prefixSuffixValue: string,
    rangeMaxValue: string,
    rangeMinValue: string,
    vieweas: string,
    category?: string
}

export interface iOptions {
    showtarget: string | boolean,
    showtargetpercentage: string | boolean,
    showthreshold: string | boolean,
    showvalue: string | boolean,
    vieweas: string,
    history: {
        size: string | number,
        units: string
    }
}

export interface iStyle {
    font: iFont
}

export interface iFont {
    color: string,
    fontFamily: string,
    fontWeight: string,
    size: string
}

export interface iScorecard {
    id: number,
    name: string,
    creationDate: number | Date,
    author: string,
    perspectives: any[]
}