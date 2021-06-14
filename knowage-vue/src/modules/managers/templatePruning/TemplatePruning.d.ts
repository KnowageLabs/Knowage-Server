export interface iFile {
    id: number,
    name: string,
    parentId?: number,
    biObjects?: Array,
    exportable?: boolean
}