export interface iOlapCustomView {
    id: number
    biobjId: number
    name: string
    isPublic: boolean
    owner: string
    description: string
    lastChangeDate: number | Date
    creationDate: number | Date
    content: string
    binaryContentId: number
}

export interface iOlapFilter {
    name: string
    uniqueName: string
    caption: string
    hierarchies: any
    selectedHierarchyUniqueName: string
    selectedHierarchyPosition: number
    axis: number
    measure: number
    positionInAxis: number
}

export interface iOlapCrossNavigationParameter {
    name: string,
    type: string
    dimension?: string,
    hierarchy?: string,
    level?: string,
    uniqueName?: string,
    value?: string
}

export interface iButton {
    category: string
    clickable: boolean
    clicked: boolean
    name: string
    visible: boolean
}

export interface iOlap {
    CALCULATED_FIELDS: any[],
    MDXWITHOUTCF: string,
    columns: any[],
    columnsAxisOrdinal: number,
    filters: iOlapFilter[],
    formulas: any[],
    hasPendingTransformations: boolean,
    mdxFormatted: string,
    modelConfig: iOlapModelConfig,
    rows: any[],
    rowsAxisOrdinal: number,
    table: string
}

export interface iOlapModelConfig {
    actualVersion: boolean,
    artifactId: number,
    axis: number,
    axisToSort: number,
    columnCount: number,
    columnSet: number,
    crossNavigation: any,
    dimensionHierarchyMap: any,
    drillType: string,
    enableDrillThrough: boolean,
    hideSpans: boolean,
    locker: any,
    pageSize: number,
    pagination: boolean,
    rowCount: number,
    rowsSet: number,
    showCompactProperties: boolean,
    showParentMembers: boolean,
    showProperties: boolean,
    sortMode: string | null,
    sortingEnabled: boolean,
    sortingPositionUniqueName: string | null,
    startColumn: number,
    startRow: number,
    status: any,
    suppressEmpty: boolean,
    toolbarClickedButtons: any[],
    toolbarMenuButtons: any[],
    toolbarVisibleButtons: string[]
    topBottomCount: number,
    whatIfScenario: boolean,
    writeBackConf: any
}

export interface iNode {
    key: string,
    id: string,
    label: string,
    children: iNode[],
    data: iFilterNode,
    style: any,
    leaf: boolean,
    selectable?: boolean,
    parent?: iNode,
    customIcon?: string
}

export interface iFilterNode {
    id: string,
    name: string,
    uniqueName: string,
    collapsed?: boolean,
    visible: boolean,
    leaf: boolean,
    children: iFilterNode[],
    text?: string,
    qtip?: string
}

export interface iParameter {
    id: number,
    label: string
    parType: string
    url: string
}

export interface iProfileAttribute {
    allowUser: boolean
    attributeDescription: string
    attributeId: number
    attributeName: string
    lovId: any
    multivalue: boolean
    syntax: any
    value: { name: string, type: string } | null
}