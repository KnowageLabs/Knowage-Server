export interface iGlossary {
    GLOSSARY_ID: number
    GLOSSARY_NM: string
    GLOSSARY_CD: string
    GLOSSARY_DS: string
}

export interface iWord {
    WORD_ID?: number
    WORD?: string
    CATEGORY?: number
    CATEGORY_NM?: string
    DESCR?: string
    FORMULA?: string
    LINK?: any[]
    SBI_GL_WORD_ATTR?: any[]
    STATE?: number
    STATE_NM?: string
    oldWord?: iWord
    NEWWORD?: boolean
    SaveOrUpdate?: string
}

export interface iNode {
    id: number
    key: number
    parentId?: number
    label: string
    children: iNode[]
    data: any
    style: any
    leaf: boolean
}
