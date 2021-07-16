export interface iNews {
    id: string,
    title: string,
    description: string,
    type: number,
    newsType?: string,
    roles?: Array,
    html?: string,
    expirationDate?: number | string,
    active?: boolean
}

export interface iRole {
    id: number,
    name: string
}