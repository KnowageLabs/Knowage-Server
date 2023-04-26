export interface iFunctionality {
    id: number,
    parentId: number,
    name: string,
    description: string,
    codType: string,
    code: string,
    prog: number
}

export interface iNode {
    id: number,
    key: number,
    parentId?: number,
    label: string,
    children: iNode[],
    data: iFunctionality
}