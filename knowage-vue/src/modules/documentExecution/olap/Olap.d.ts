export interface iOlapCustomView {
    id: number
    biobjId: number
    name: string
    isPublic: boolean
    owner: string
    description: string
    lastChangeDate: number | Date
    creationDate: number | Date
    content: string
    binaryContentId: number
}

export interface iOlapFilter {
    name: string
    uniqueName: string
    caption: string
    hierarchies: any
    selectedHierarchyUniqueName: string
    selectedHierarchyPosition: number
    axis: number
    measure: number
    positionInAxis: number
}

export interface iOlapCrossNavigationParameter {
    name: string,
    type: string
    dimension?: string,
    hierarchy?: string,
    level?: string,
    uniqueName?: string,
    value?: string
}

export interface iButton {
    category: string
    clickable: boolean
    clicked: boolean
    name: string
    visible: boolean
}