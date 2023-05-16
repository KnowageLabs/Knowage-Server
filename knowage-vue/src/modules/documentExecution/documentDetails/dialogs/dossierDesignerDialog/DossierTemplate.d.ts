export interface iDossierTemplate {
    type: string
    prefix?: string
    name: string
    placeholders: iPlaceholder[]
    downloadable?: boolean
    uploadable?: boolean
}

export interface iPlaceholder {
    imageName: string
    label?: string
    source: string
    parameters: any[]
    views: iViews
    sheetHeight: number
    sheetWidth: number
    deviceScaleFactor: number
}

export interface iViews {
    selected: iView
    availableViews: iView[]
}

export interface iView {
    name: string
    creationDate: Date
}

export interface iDossierDriver {
    label: string
    type: string
    dossierUrlName?: string
    urlName?: string
    urlNameDescription?: string
    value?: string
    inherit?: boolean
}
