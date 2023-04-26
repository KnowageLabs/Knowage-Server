export interface iNode {
    key: number | string,
    icon: string,
    id: number,
    parentId?: any,
    label: string,
    children?: iNode[],
    selectable?: Boolean,
    data: string | any,
    prog: number,
    parentFolder?: iNode
}