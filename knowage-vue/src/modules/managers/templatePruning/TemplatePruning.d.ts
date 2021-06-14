export interface iFile {
    id: number,
    name: string,
    parentId?: number,
    biObjects?: Array,
    exportable?: boolean
}

export interface iNode {
    key: number | string,
    icon: string,
    id: number,
    parentId?: number,
    label: string,
    children?: iNode[],
    data: string | any
}