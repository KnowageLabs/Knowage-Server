export interface IMenuItem {
    label: string
    url?: string
    to?: string
    iconCls?: string
    items?: Array<MenuItem> | Array<Array<MenuItem>>
    conditionedView?: string
    badge?: number
    command?: string
}
