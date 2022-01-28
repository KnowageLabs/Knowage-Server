export interface iMultitenant {
    MULTITENANT_ID?: string
    MULTITENANT_NAME: string
    MULTITENANT_THEME?: string
    MULTITENANT_IMAGE?: any
}

export interface iTenantToSave {
    MULTITENANT_ID?: string
    MULTITENANT_NAME: string
    MULTITENANT_THEME?: string
    MULTITENANT_IMAGE?: any
    DS_LIST: Array
    PRODUCT_TYPE_LIST: Array
}