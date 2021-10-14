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
    data: any,
    style: any,
    leaf: boolean,
    selectable?: boolean,
    parent?: iNode,
    itemType?: string
}

export interface iNavigationTableItem {
    id: number,
    label: string,
    organization?: string,
    type: string
}

export interface iLinkTableItem {
    id: number,
    name: strig,
    description: string,
    type: string,
    author: string,
    itemType: string,
    organization?: string
}

export interface iWord {
    WORD_ID: number,
    WORD: string
}