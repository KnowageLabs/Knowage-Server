export interface IKnCalculatedField {
    colName?: string
    type?: string
    formula?: string
}

export interface IKnCalculatedFieldFunction {
    category: string
    formula: string
    label: string
    name: string
    help: string
}
