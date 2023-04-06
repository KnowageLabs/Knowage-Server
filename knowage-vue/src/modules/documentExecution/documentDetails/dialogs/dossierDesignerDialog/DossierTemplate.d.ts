export interface iDossierTemplate {
    type: string
    prefix?: string
    fileName: string
    placeholders: iPlaceholder[]
    downloadable: boolean
    uploadable: boolean
}

export interface iPlaceholder {
    name: string
    documentLabel?: string
    source: string
    parameters?: iDriver[]
    views?: iView[]
}

export interface iView {
    name: string
    creationDate: Date
}

export interface iDriver {
    label: string
    type: string
    dossier_url_name?: string
    url_name?: string
    url_name_description?: string
    value?: string
    inherit?: boolean
}
