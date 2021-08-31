export interface iGlossary {
    GLOSSARY_ID: number,
    GLOSSARY_NM: string,
    GLOSSARY_CD: string,
    GLOSSARY_DS: string
}

export interface iWord {
    WORD_ID: number,
    WORD: string
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
}
