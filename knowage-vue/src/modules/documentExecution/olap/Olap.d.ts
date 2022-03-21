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