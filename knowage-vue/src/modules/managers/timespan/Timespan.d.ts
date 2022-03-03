export interface iTimespan {
    name: string,
    id?: number,
    type: "time" | "temporal",
    definition: { from: string, to: string, fromLocalized?: string, toLocalized?: string }[],
    category: string,
    staticFilter?: boolean,
    commonInfo?: string,
    isnew?: boolean
}

export interface iCategory {
    VALUE_NM: string,
    VALUE_DS: string,
    VALUE_ID: number,
    VALUE_CD: string
}