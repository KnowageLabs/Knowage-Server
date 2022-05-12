export interface iLayer {
    layerId: number
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
    layerOrder: number
    category_id: any
    category: any
    roles: any
    properties: any
    filebody: any
}

export interface iFilter {
    property: string
}