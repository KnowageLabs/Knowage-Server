export interface iNavigation {
    id?: number
    name?: string
    description?: string
    breadcrumb?: string
    type?: number
    fromDoc?: string
    fromDocId?: number
    toDoc?: string
    toDocId?: number
    fixedValue?: boolean
    popupOptions?: any
}
