export interface iMenuNode {
    adminsMenu: Boolean
    clickable: Boolean
    code: number | null
    custIcon: iIcon | null
    depth: string | null
    descr: string | null
    externalApplicationUrl: string | null
    functionality: any | null
    groupingMenu: string | null
    hasChildren: Boolean
    hideSliders: Boolean
    hideToolbar: Boolean
    icon: iIcon | null
    iconCls: string | null
    iconPath: string | null
    initialPath: any | null
    level: number | null
    linkType: string | null
    lstChildren: []
    children: []
    menuId: number
    name: string
    document: string | null
    objId: number | null
    objParameters: string | null
    parentId: number | null
    prog: number
    roles: iRole[]
    snapshotHistory: number | null
    snapshotName: string | null
    staticPage: string | null
    subObjName: number | null
    url: string | null
    viewIcons: Boolean
    menuNodeContent: any | null
}

export interface iIcon {
    id: number | null
    category: string
    className: string
    src: string | null
    label: string
    unicode: string | null
    visible: Boolean
}

export interface iRole {
    id: number | null
    name: string
    value: string
}

export interface iDocument {
    DOCUMENT_ID: number | null
    DOCUMENT_LABEL: string
    DOCUMENT_NAME: string
    DOCUMENT_DESCR: string
    DOCUMENT_AUTH: string
}
