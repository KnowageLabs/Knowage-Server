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
