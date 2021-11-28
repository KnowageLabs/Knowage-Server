export interface iDocument {
    creationDate: string
    creationUser: string
    dataSetId: number
    dataSetLabel: any
    dataSourceLabel: string
    datasetsIds: any
    description: string
    docVersion: any
    drivers: any
    engine: string
    functionalities: any
    id: number
    label: string
    lockedByUser: string
    metamodelDrivers: any
    name: string
    objMetaDataAndContents: any
    outputParameters: any
    parametersRegion: string
    previewFile: string
    profiledVisibility: string
    public: boolean
    refreshSeconds: number
    stateCode: string
    stateCodeStr: string
    tenant: string
    typeCode: string
    visible: boolean
    modelLocked: boolean
}

export interface iDataSource {
    descr: string
    dialectName: string
    driver: string
    dsId: number
    hibDialectClass: string
    jndi: string
    label: string
    multiSchema: boolean
    owner: string
    readOnly: boolean
    schemaAttribute: string
    urlConnection: string
    user: string
    writeDefault: boolean
}

export interface iAnalyticalDriver {
    checks: any
    defaultFormula: string
    description: string
    functional: boolean
    id: number
    label: string
    length: number
    mask: string
    modality: string
    modalityValue: any
    modalityValueForDefault: any
    modalityValueForMax: any
    name: string
    selectedLayer: any
    selectedLayerProp: any
    temporal: boolean
    type: string
    typeId: number
    valueSelection: any
}

export interface iDriver {
    colSpan: any
    hasValidValues: boolean
    id: number
    iterative: boolean
    label: string
    maxValue: any
    modifiable: number
    multivalue: boolean
    parID: number
    parameter: any
    parameterUrlName: string
    parameterValues: any
    parameterValuesDescription: any
    parameterValuesRetriever: any
    priority: number
    prog: number
    required: boolean
    thickPerc: any
    transientParmeters: boolean
    visible: boolean
    newDriver?: any
    biObjectID?: number
    biMetaModelID?: number
}

export interface iEngine {
    biobjTypeId: number
    className: string
    criptable: number
    description: string
    dirUpload: any
    dirUsable: any
    driverName: string
    engineTypeId: number
    id: number
    label: string
    name: string
    secondaryUrl: any
    url: string
    useDataSet: boolean
    useDataSource: boolean
}

export interface iTemplate {
    active: boolean
    binId: number
    biobjId: number
    creationDate: number
    creationUser: string
    dimension: any
    id: number
    name: string
    prog: number
}

export interface iAttribute {
    allowUser: boolean
    attributeDescription: string
    attributeId: number
    attributeName: string
    lovId: any
    multivalue: boolean
    syntax: any
    value: any
}

export interface iVisualDependency {
    compareValue: string
    id?: number
    operation: string
    parFatherId: number
    parFatherUrlName: string
    parId: number
    prog: number
    viewLabel: string
}
