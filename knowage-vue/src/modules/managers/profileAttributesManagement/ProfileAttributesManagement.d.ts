export interface iAttribute {
    attributeId: number | null;
    attributeName: string;
    attributeDescription: string | null;
    allowUser: Boolean | null;
    multivalue: Boolean | null;
    syntax: Boolean | null;
    lovId: number | null;
    value: iValue | {};
}

export interface iLov {
    id: number | null;
    name: string;
    description: string | null;
    label: string | null;
    lovProvider: JSON | null;
    lovProvider: string | null;
    itypeCd: string | null;
    itypeId: string | null;
    lovProviderJSON: JSON | null;
}

export interface iValue {
    name: string
    type: string
}