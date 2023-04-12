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
    views: iView[]
}

export interface iView {
    name: string
    creationDate: Date
}

export interface iDossierDriver {
    label: string
    type: string
    dossier_url_name?: string
    url_name?: string
    url_name_description?: string
    value?: string
    inherit?: boolean
}
