export interface iRole {
    id?: string
    name: string
    description?: string,
    roleTypeCD?: string,
    isPublic?: boolean,
    roleMetaModelCategories?: Array
}

export interface iCategory {
    categoryId: number
    categoryName: string
}