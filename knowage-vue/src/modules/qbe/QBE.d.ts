export interface iQBE {
    actions: { name: string, description: string }[],
    canLoadData: boolean,
    catTypeId: any,
    catTypeVn: any,
    dateIn: string | Date,
    description: string | null,
    dsTypeCd: string,
    dsVersions: any[],
    endDate: string | Date | null,
    id: number,
    isIterable: boolean,
    isPersisted: boolean,
    isPersistedHDFS: boolean,
    isRealtime: boolean,
    isScheduled: boolean,
    label: string,
    meta: { dataset: { pname: string, pvalue: string }[], columns: { column: string, pname: string, pvalue: string }[] },
    name: string,
    owner: string,
    pars: any[],
    persistTableName: string,
    pivotColName: string | null,
    pivotColValue: string | null,
    pivotIsNumRows: boolean,
    pivotRowName: string | null,
    qbeDataSource: string,
    qbeDataSourceId: number,
    qbeDatamarts: string,
    qbeJSONQuery: any,
    schedulingCronLine: any,
    scopeCd: string,
    scopeId: number,
    startDate: string | Date | null,
    tags: any[],
    trasfTypeCd: any,
    usedByNDocs: number,
    userIn: string,
    versNum: number
}

export interface iQuery {
    calendar: any,
    distinct: boolean,
    expression: any,
    fields: iField[],
    filters: iFilter[],
    graph: any[],
    havings: any[],
    id: string,
    isNestedExpression: boolean,
    name: string,
    relationRoles: any[],
    subqueries: any[]
}

export interface iField {
    alias: string
    color: string
    dataType: string
    distinct: boolean
    entity: string
    field: string
    fieldType: string
    format: string
    funct: string
    group: boolean
    iconCls: string
    id: string
    inUse: boolean
    include: boolean
    leaf: boolean
    longDescription: string
    order: string
    type: string
    visible: boolean
}

export interface iQueryResult {
    metaData: {
        totalProperty: string,
        root: string,
        id: string,
        fields: string | iQueryField[]
    },
    results: number,
    rows: any[]
}

export interface iQueryField {
    name: string,
    header: string,
    dataIndex: string,
    type: string,
    precision?: number,
    scale?: number,
    multiValue: boolean
}

export interface iFilter {
    booleanConnector: string,
    color: string
    deleteButton: boolean,
    entity: string,
    filterDescripion: string,
    filterId: string,
    filterInd: number,
    leftOperandAlias?: string,
    leftOperandDataType: string,
    leftOperandDefaultValue: any,
    leftOperandDescription: string,
    leftOperandLastValue: any,
    leftOperandLongDescription: string,
    leftOperandType: string,
    leftOperandValue: string,
    operator: string,
    promptable: boolean,
    rightOperandAlias?: any,
    rightOperandDataType: string,
    rightOperandDefaultValue: any[],
    rightOperandDescription: string,
    rightOperandLastValue: string[],
    rightOperandLongDescription: string,
    rightOperandType: string,
    rightOperandValue: string[],
    rightType: string,
    hasParam?: boolean,
    paramName?: string,
    leftOperandAggregator?: string,
    rightOperandAggregator?: string
}