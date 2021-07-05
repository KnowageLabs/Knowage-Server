export interface iLanguage {
    iso3code: string,
    language: string,
    languageTag: string,
    defaultLanguage?: boolean
}

export interface iMessage {
    id?: number
    label: string
    languageCd?: number
    message: string
    dirty?: boolean
}