export interface iNews {
    id: string,
    title: string,
    description: string,
    type: number,
    roles?: Array,
    html?: string,
    expirationDate?: number | string,
    active?: boolean
}

export interface iRole {
    id: number,
    name: string
}