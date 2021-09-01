export interface iGlossary {
    GLOSSARY_ID?: number
    GLOSSARY_NM: string
    GLOSSARY_CD: string
    GLOSSARY_DS: string,
    NEWGLOSS?: boolean,
    SBI_GL_CONTENTS?: any[],
    SaveOrUpdate?: string
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
    SaveOrUpdate?: string,
    PARENT?: any
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

export interface iContent {
    CONTENT_ID: number | string,
    CONTENT_NM: string,
    CONTENT_CD: string,
    CONTENT_DS: string,
    SaveOrUpdate: string,
    GLOSSARY_ID?: number,
    NEWCONT?: boolean,
    PARENT_ID?: number,
}
