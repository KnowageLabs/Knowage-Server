export interface IKnListBoxOptions {
    avatar?: IAvatar
    titleField?: string
    textField?: string
    filterFields: Array<string>
    sortFields: Array<string>
    interaction?: object
    buttons: Array<Ibutton>
    defaultSortField: string
}

export interface IAvatar {
    property: string
    icons: object
}

export interface Ibutton {
    label: string
    className?: string
    emits: string
    condition?: string
}
