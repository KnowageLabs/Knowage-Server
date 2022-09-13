export interface ISheet {
    label: string
    icon?: string
    widgets: Array<object>
}

export interface IWidget {
    id?: string
    dataset: number
    type: string
    columns: IWidgetColumn[]
    conditionalStyles: any[]  // TO REMOVE
    interactions: any[]  // TO REMOVE
    theme: string  // TO REMOVE
    style: any  // TO REMOVE
    settings: ITableWidgetSettings | any
    new?: boolean
}

export interface ITableWidgetSettings {
    sortingColumn?: string,
    sortingOrder?: string,
    updatable: boolean,
    clickable: boolean,
    conditionalStyles: ITableWidgetConditionalStyle[],
    configuration: ITableWidgetConfiguration,
    interactions: ITableWidgetInteraction,
    pagination: ITableWidgetPagination,
    style: ITableWidgetStyle,
    tooltips: ITableWidgetTooltips,
    visualization: ITableWidgetVisualization,
    responsive: ITableWidgetResponsive,
}

export interface ITableWidgetConditionalStyle {
    target: string
    condition: {
        type: string,
        variable: string,
        operator: string,
        value: string
    },
    properties: {
        "font-size": string,
        icon: string
    }
}
export interface ITableWidgetConfiguration {
    columnGroups: ITableWidgetColumnGroups,
    exports: ITableWidgetExports
    headers: ITableWidgetHeaders
    rows: ITableWidgetRows
    summaryRows: ITableWidgetSummaryRows
}

export interface ITableWidgetColumnGroups {
    enabled: boolean,
    groups: ITableWidgetColumnGroup[]
}

export interface ITableWidgetColumnGroup {
    id: string,
    label: string,
    columns: string[]
}

export interface ITableWidgetExports {
    pdf: {
        enabled: boolean,
        custom: {
            height: number,
            width: number,
            enabled: boolean
        },
        a4landscape: boolean,
        a4portrait: boolean
    }
}

export interface ITableWidgetHeaders {
    enabled: boolean,
    enabledMultiline: boolean,
    custom: {
        enabled: boolean,
        rules: ITableWidgetHeadersRule[]
    }
}

export interface ITableWidgetHeadersRule {
    target: string[],
    action: string,
    compareType?: string,
    variable?: string,
    value?: string,
    parameter?: string
}

export interface ITableWidgetRows {
    indexColumn: boolean,
    rowSpan: {
        enabled: boolean,
        column: string
    }
}

export interface ITableWidgetSummaryRows {
    enabled: boolean,
    list: ITableWidgetSummaryRow[],
    style: { pinnedOnly: boolean }
}

export interface ITableWidgetSummaryRow {
    label: string,
    aggregation: string
}

export interface ITableWidgetInteraction {
    crosssNavigation: {
        "enabled": boolean,
        "type": string,
        "column": string,
        "name": string,
        "parameters": ITableWidgetParameter[]
    },
    link: {
        "enabled": boolean,
        "type": string,  //column, row, icon
        "icon": string,
        "baseurl": string,
        "action": string,
        "parameters": ITableWidgetParameter[]
    },
    preview: ITableWidgetPreview,
    selection: ITableWidgetSelection
}

export interface ITableWidgetParameter {
    "enabled"?: boolean,
    "name": string,
    "type": string,
    "value"?: string,
    "column"?: string,
    "driver"?: string
}

export interface ITableWidgetPreview {
    "enabled": boolean,
    "type": string,
    "parameters": ITableWidgetParameter[],
    "dataset": number,
    "column": string,
    "directDownload": boolean
}

export interface ITableWidgetSelection {
    "enabled": boolean,
    "modalColumn": string,
    "multiselection": {
        "enabled": boolean,
        "properties": {
            "background-color": string,
            "color": string
        }
    }
}

export interface ITableWidgetPagination {
    "enabled": boolean,
    "itemsNumber": number
}

export interface ITableWidgetStyle {
    borders: ITableWidgetBordersStyle,
    columns: ITableWidgetColumnStyle[],
    columnGroups: ITableWidgetColumnStyle[],
    headers: ITawbleWidgetHeadersStyle,
    padding: ITableWidgetPaddingStyle,
    rows: ITableWidgetRowsStyle,
    shadows: ITableWidgetShadowsStyle,
    summary: ITableWidgetSummaryStyle
}

export interface ITableWidgetBordersStyle {
    enabled: boolean,
    properties: {
        "border-bottom-left-radius": string,
        "border-bottom-right-radius": string,
        "border-style": string,
        "border-top-left-radius": string,
        "border-top-right-radius": string,
        "border-width": string,
        "border-color": string
    }
}

export interface ITableWidgetColumnStyle {
    allColumnSelected?: boolean,
    target: string[],
    properties: {
        "background-color": string,
        color: string,
        "justify-content": string,
        "font-size": string,
        "font-family": string,
        "font-style": string,
        "font-weight": string
    }

}

export interface ITawbleWidgetHeadersStyle {
    height: number,
    properties: {
        "background-color": string,
        color: string,
        "justify-content": string,
        "font-size": string,
        "font-family": string,
        "font-style": string,
        "font-weight": string
    }
}

export interface ITableWidgetPaddingStyle {
    "enabled": boolean,
    "properties": {
        "padding-top": string,
        "padding-left": string

    }
}

export interface ITableWidgetRowsStyle {
    height: number,
    selectionColor: string,
    multiselectable: boolean,
    alternatedRows: {
        enabled: boolean,
        evenBackgroundColor: string,
        oddBackgroundColor: string

    }
}

export interface ITableWidgetShadowsStyle {
    "enabled": boolean,
    "properties": {
        "box-shadow": string
    }
}

export interface ITableWidgetSummaryStyle {
    "font-family": string,
    "font-style": string,
    "font-size": string,
    "font-weight": string,
    color: string,
    "background-color": string,
    'justify-content': string
}

export interface ITableWidgetTooltips {
    "target": string,
    "enabled": true,
    "prefix": string,
    "suffix": string,
    "precision": string,
    "header": {
        "enabled": boolean,
        "text": string
    }
}

export interface ITableWidgetVisualization {
    types: ITableWidgetVisualizationType[],
    visibilityConditions: ITableWidgetVisibilityCondition[]
}
export interface ITableWidgetVisualizationType {
    target: string[],
    type: string,
    precision?: number,
    prefix?: string,
    suffix?: string,
    pinned?: string,
    min?: number,
    max?: number,
    alignment?: string,
    color?: string,
    "background-color"?: string,
    allColumnSelected?: boolean
}

export interface ITableWidgetVisibilityCondition {
    target: string[],
    hide: boolean,
    hidePdf: boolean,
    condition: {
        type: string,
        variable?: string,
        variableValue?: string,
        operator?: string,
        value?: string
    }
}

export interface ITableWidgetResponsive {
    xs: boolean,
    sm: boolean,
    md: boolean,
    lg: boolean,
    xl: boolean
}


export interface IWidgetColumn {
    id?: string
    columnName: string
    alias: string
    type: string
    fieldType: string
    multiValue: boolean,
    aggregation?: string,
    style?: any,  // ??? 
    enableTooltip?: boolean, // ???
    visType?: string   // ???
    filter?: IWidgetColumnFilter
}

export interface IWidgetColumnFilter {
    enabled: boolean
    operator: string
    value: string,
    value2?: string
}

export interface IWidgetEditorDataset {
    id: number
    label: string
    cache: boolean
    parameters?: any[],
    drivers?: any[],
    indexes?: any[]
}
export interface IWidgetPickerType {
    cssClass: string
    descKey: string
    img: string
    name: string
    tags: Array<string>
    type: string
}

export interface IDatasetOptions {
    aggregations: {
        measures: any[]
        categories: IDatasetOptionCategory[]
        dataset: string
    }
    parameters: any
    selections: any
    indexes: any[]
}

export interface IDatasetOptionCategory {
    id: string
    alias: string
    columnName: string
    orderType: string
    funct: string
}

export interface IDatasetColumn {
    dataset?: number
    name: string
    alias: string
    type: string
    properties: any
    fieldType: string
    multiValue: boolean
    precision: number
    scale: number
    personal: boolean
    decript: boolean
    subjectId: boolean
}

export interface IDataset {
    id: {
        dsId: number
        versionNum: number
        organization: string
    }
    name: string
    description: string
    label: string
    active: boolean
    type: string
    configuration: any
    pivotColumnName: string
    pivotRowName: string
    pivotColumnValue: string
    numRows: boolean
    persisted: boolean
    persistedHDFS: boolean
    persistTableName: string
    owner: string
    userIn: any
    userUp: any
    userDe: any
    sbiVersionIn: any
    sbiVersionUp: any
    sbiVersionDe: any
    metaVersion: any
    timeIn: any
    timeUp: any
    timeDe: any
    scope: any
    federation: any
    tags: any[]
    scopeId: number
    transformerId: number
    metadata: {
        fieldsMeta: any[]
        properties: any
    }
    categoryId: number
    parameters: IDatasetParameters[]
    isRealtime: boolean
    isCachingSupported: boolean
    isIterable: boolean
    isNearRealtimeSupported: boolean,
    cache?: boolean,
    indexes?: any[],
    drivers?: any[]
}

interface IDatasetParameters {
    name: string
    type: string
    defaultValue: string
    multiValue: boolean
}

interface IAssociation {
    id: string
    fields: IAssociationField[]
    validation?: {
        isValid: boolean
        msg: string
    }
}

interface IAssociationField {
    column: string
    dataset: number
}

export interface IModelDataset {
    id: number
    cache: boolean
    indexes: string[]
    parameters: IModelDatasetParameter[],
    drivers: any[]
}

interface IModelDatasetParameter {
    multivalue: boolean
    name: string
    type: string
    value: string
}

export interface IIcon {
    id: number
    name: string
    value: string
}

export interface IWidgetStyleToolbarModel {
    'font-weight'?: string,
    'font-style'?: string,
    'font-size'?: string,
    'font-family'?: string,
    'justify-content'?: string,
    color?: string,
    'background-color'?: string
}