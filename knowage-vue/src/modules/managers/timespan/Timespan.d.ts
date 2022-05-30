export interface iTimespan {
    name: string,
    id?: number,
    type: "time" | "temporal",
    definition: iInterval[],
    category: string,
    staticFilter?: boolean,
    commonInfo?: string,
    isnew?: boolean,
    isCloneable?: boolean
}

export interface iInterval {
    from: any,
    to: any,
    fromLocalized?: string,
    toLocalized?: string
}

export interface iCategory {
    VALUE_NM: string,
    VALUE_DS: string,
    VALUE_ID: number,
    VALUE_CD: string
}