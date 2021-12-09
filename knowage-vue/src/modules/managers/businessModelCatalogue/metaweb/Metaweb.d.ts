export interface iPhysicalModel {
    name: string,
    properties: any[],
    comment: string,
    type: string,
    columns: iColumn[],
    foreignKeys: iForeignKey[],
    type: string
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
    primaryKey: boolean,
    type?: string,
    foreignKeys?: iForeignKey[]
}

export interface iForeignKey {
    description: string,
    destinationColumns: iColumn[],
    destinationName: string | null,
    destinationTableName: string | null,
    id: string | null,
    name: string,
    properties: any[],
    sourceColumns: iColumn[],
    sourceName: string,
    uniqueName: string | null
}

export interface iChangedData {
    missingColumns: string[]
    missingTables: string[]
    removingItems: string[]
}

export interface iBusinessModel {
    calculatedBusinessColumns: any[],
    columns: any[],
    name: string,
    physicalTable: { physicalTableIndex: number },
    properties: any[],
    relationships: iRelationship[],
    simpleBusinessColumns: iSimpleBusinessColumn[],
    uniqueName: string,
    physicalColumn?: any
}

export interface iSimpleBusinessColumn {
    description: string | null,
    filteredByProfileAttribute: boolean
    filteredByRoleVisibility: boolean
    id: number | null
    identifier: boolean
    name: string
    partOfCompositeIdentifier: boolean
    physicalColumn: iColumn,
    properties: any[],
    uniqueName: string
}

export interface iRelationship {
    description: string | null
    destinationColumns: iSimpleBusinessColumn[]
    destinationSimpleBusinessColumns: iSimpleBusinessColumn[]
    destinationTableName: string
    id: number | null
    name: string
    physicalForeignKeyName: string
    properties: any[]
    sourceColumns: iSimpleBusinessColumn[]
    sourceSimpleBusinessColumns: iSimpleBusinessColumn[]
    sourceTableName: string
    uniqueName: string
}
