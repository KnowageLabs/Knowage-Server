export interface iPhysicalModel {
    name: string,
    properties: any[],
    comment: string,
    type: string,
    columns: iColumn[],
    foreignKeys: any[]
}

export interface iColumn {
    id: number | null,
    name: string,
    uniqueName: string | null,
    description: string | null,
    properties: any[],
    comment: string,
    dataType: string,
    typeName: string,
    size: number,
    octectLength: number,
    decimalDigits: number,
    radix: number,
    defaultValue: string | null,
    nullable: boolean,
    position: number,
    tableName: string,
    partOfCompositePrimaryKey: boolean,
    markedDeleted: boolean,
    primaryKey: boolean
}