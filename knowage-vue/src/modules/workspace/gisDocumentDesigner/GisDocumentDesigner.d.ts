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

export interface iLayer {
    layerId: int
    name: string
    descr: string
    type: string
    label: string
    baseLayer: boolean
    layerDef: string
    pathFile: string
    layerLabel: string
    layerName: string
    layerIdentify: string
    layerURL: any
    layerOptions: any
    layerParams: any
    layerOrder: int
    category_id: any
    category: any
    roles: any
    properties: Array
    filebody: any
}

export interface iDriver {
    id: number
    label: string
    parType: string
    url: string
}
