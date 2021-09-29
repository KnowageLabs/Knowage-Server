export interface iFunction {
    id?: string,
    name: string,
    description: string,
    benchmarks: string,
    language: string,
    family?: string,
    onlineScript: string,
    offlineScriptTrainModel: string,
    offlineScriptUseModel: string,
    owner: string,
    label: string,
    type: string,
    keywords: string[],
    inputVariables: iInputVariable[],
    inputColumns: iInputColumn[],
    outputColumns: iOutputColumn[],
    trainModelCode?: string,
    useModelCode?: string,
    functionFamily?: string
}

export interface iInputVariable {
    name: string,
    type: string,
    value: string
}

export interface iInputColumn {
    name: string,
    type: string,
    datasetColumn?: string
}

export interface iOutputColumn {
    name: string,
    fieldType: string,
    type: string
}

export interface iFunctionType {
    valueId: number,
    valueCd: string,
    valueName: string,
    valueDescription: string,
    domainCode: string,
    domainName: string,
    translatedValueDescription: string,
    translatedValueName: string,
    active?: boolean
}

export interface iDataset {
    id: number,
    name: string,
    label: string,
    dsType: string,
    meta?: any
}

export interface iPythonConfiguration {
    id: number,
    label: string,
    name: strign,
    description: string,
    valueCheck: string,
    valueTypeId: number,
    category: string,
    active: boolean
}