export interface iLov {
    id: number
    name: string
    description: string
    label: string
    lovProvider: any
    itypeId: string
    itypeCd: string
    lovProviderJSON: any
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

export interface iDocument {
    creationDate: string
    creationUser: string
    dataSetId: any
    dataSetLabel: any
    dataSourceLabel: any
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
    previewFile: any
    profiledVisibility: any
    public: boolean
    refreshSeconds: number
    stateCode: string
    stateCodeStr: string
    tenant: string
    typeCode: string
    visible: boolean
}
