export interface iParameter {
    urlName: string,
    metadata: {
        colsMap: any
    },
    visible: boolean,
    valueSelection: string,
    showOnPanel: string,
    driverUseLabel: string,
    label: string,
    driverDefaultValue: { value: string | number, desc: string }[],
    parameterValue: { value: string | number, description: string }[],
    type: string,
    driverLabel: string,
    mandatory: boolean,
    allowInternalNodeSelection: boolean,
    multivalue: boolean,
    dependencies: {
        data: any[],
        visual: any[],
        lov: any[]
    },
    selectionType: string,
    id: number,
    parameterDescription: string[],
    dependentParameters?: iParameter[]
}


export interface iDocument {
    creationDate: string
    creationUser: string
    dataSetId: number | null
    dataSetLabel: number | null
    dataSourceLabel: number | null
    datasetsIds: null
    description: string
    docVersion: null
    drivers: any[]
    engine: string
    functionalities: string[]
    id: number
    label: string
    lockedByUser: string
    metamodelDrivers: any
    name: string
    objMetaDataAndContents: any
    outputParameters: any[]
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
}
