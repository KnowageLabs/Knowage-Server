export interface iGlossary {
    GLOSSARY_ID: number,
    GLOSSARY_NM: string
}

export interface iNode {
    id: number,
    key: number,
    parentId?: number,
    label: string,
    children: iNode[],
    data: iFunctionality,
    style: any,
    leaf: boolean
}