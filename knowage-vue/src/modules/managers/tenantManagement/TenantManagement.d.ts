export interface iTenant {
    TENANT_ID?: string
    TENANT_NAME: string
    TENANT_THEME?: string
    TENANT_IMAGE?: any
    TENANT_IMAGE_WIDE?: any
}

export interface iTenantToSave {
    TENANT_ID?: string
    TENANT_NAME: string
    TENANT_THEME?: string
    TENANT_IMAGE?: any
    TENANT_IMAGE_WIDE?: any
    DS_LIST: Array
    PRODUCT_TYPE_LIST: Array
}