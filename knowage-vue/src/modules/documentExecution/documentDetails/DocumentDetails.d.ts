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
    parameter: iAnalyticalDriver
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
    biObjectID?: number
    biMetaModelID?: number
    newDriver?: any
    numberOfErrors?: number
    isChanged?: boolean
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

export interface iParType {
    domainCode: string
    domainName: string
    translatedValueDescription: string
    translatedValueName: string
    valueCd: string
    valueDescription: string
    valueId: number
    valueName: string
}

export interface iDateFormat {
    domainCode: string
    domainName: string
    translatedValueDescription: string
    translatedValueName: string
    valueCd: string
    valueDescription: string
    valueId: number
    valueName: string
}

export interface iFolder {
    biObjects: any
    codType: string
    code: string
    createRoles: any
    description: string
    devRoles: any
    execRoles: any
    id: number
    name: string
    parentId: null
    path: string
    prog: number
    testRoles: any
}

export interface iMetaSource {
    location: any
    name: string
    role: any
    sourceCatalogue: any
    sourceId: number
    sourceSchema: string
    type: string
    url: string
}

export interface iTableSmall {
    deleted: boolean
    name: string
    tableId: number
}

export interface iOutputParam {
    biObjectId: number
    formatCode: string | null
    formatValue: string | null
    id: number
    isUserDefined: boolean
    name: string
    tempId?: number
    type: iParType
    isChanged?: boolean
    numberOfErrors?: any
}

export interface iDocumentType {
    domainCode: string
    domainName: string
    translatedValueDescription: string
    translatedValueName: string
    valueCd: string
    valueDescription: string
    valueId: number
    valueName: string
}

export interface iMondrianSchema {
    id: number,
    currentContentId: number,
    name: string,
    description: string,
    type: string,
    modelLocked: boolean,
    modelLocker: string | null
}

export interface iXMLATemplate {
    address: string;
    parameters: { name: string, value: string }[]
}

export interface iMondrianTemplate {
    id: number,
    mondrianSchema: string,
    mondrianSchemaId: number,
    mdxQuery: string,
    mondrianMdxQuery: string
}