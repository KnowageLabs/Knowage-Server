export interface iMultitenant {
    MULTITENANT_ID?: string
    MULTITENANT_NAME: string
    MULTITENANT_THEME?: string,
}

export interface iTenantToSave {
    MULTITENANT_ID?: string
    MULTITENANT_NAME: string
    MULTITENANT_THEME?: string
    DS_LIST: Array
    PRODUCT_TYPE_LIST: Array
}