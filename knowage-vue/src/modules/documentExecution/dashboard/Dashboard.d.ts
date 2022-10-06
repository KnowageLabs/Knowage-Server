export interface IDashboard {
    sheets: [],
    widgets: ITableWidget[],
    configuration: {
        id: string,
        name: string,
        label: string,
        description: string,
        associations: IAssociation[],
        datasets: IModelDataset[],
        variables: IVariable[],
        themes: any
    },
    version: string
}



export interface IDatasetParameter {
    name: string
    type: 'static' | 'dynamic'
    value: string
}

export interface ISheet {
    label: string
    icon?: string
    widgets: Array<object>
}

export interface IWidget {
    id?: string
    dataset: number | null
    type: string
    columns: IWidgetColumn[]
    settings: ITableWidgetSettings
    new?: boolean
}

export interface ITableWidgetSettings {
    sortingColumn?: string
    sortingOrder?: string
    updatable: boolean
    clickable: boolean
    conditionalStyles: ITableWidgetConditionalStyles
    configuration: ITableWidgetConfiguration
    interactions: ITableWidgetInteractions
    pagination: ITableWidgetPagination
    style: ITableWidgetStyle
    tooltips: ITableWidgetTooltipStyle[]
    visualization: ITableWidgetVisualization
    responsive: ITableWidgetResponsive
}

export interface ITableWidgetConditionalStyles {
    enabled: boolean
    conditions: ITableWidgetConditionalStyle[]
}

export interface ITableWidgetConditionalStyle {
    target: string
    applyToWholeRow: boolean
    condition: {
        type: string
        variable?: string
        parameter?: string
        operator: string
        value: string
    }
    properties: {
        'justify-content': string
        'font-family': string
        'font-size': string
        'font-style': string
        'font-weight': string
        color: string
        'background-color': string
        icon: string
    }
}
export interface ITableWidgetConfiguration {
    columnGroups: ITableWidgetColumnGroups
    exports: ITableWidgetExports
    headers: ITableWidgetHeaders
    rows: ITableWidgetRows
    summaryRows: ITableWidgetSummaryRows
    customMessages: ITableWidgetCustomMessages
}

export interface ITableWidgetColumnGroups {
    enabled: boolean
    groups: ITableWidgetColumnGroup[]
}

export interface ITableWidgetColumnGroup {
    id: string
    label: string
    columns: string[]
}

export interface ITableWidgetExports {
    pdf: {
        enabled: boolean
        custom: {
            height: number
            width: number
            enabled: boolean
        }
        a4landscape: boolean
        a4portrait: boolean
    }
    showExcelExport: boolean
    showScreenshot: boolean
}

export interface ITableWidgetHeaders {
    enabled: boolean
    enabledMultiline: boolean
    custom: {
        enabled: boolean
        rules: ITableWidgetHeadersRule[]
    }
}

export interface ITableWidgetHeadersRule {
    target: string[]
    action: string
    compareType?: string
    variable?: string
    value?: string
    parameter?: string
}

export interface ITableWidgetRows {
    indexColumn: boolean
    rowSpan: {
        enabled: boolean
        column: string
    }
}

export interface ITableWidgetSummaryRows {
    enabled: boolean
    list: ITableWidgetSummaryRow[]
    style: { pinnedOnly: boolean }
}

export interface ITableWidgetCustomMessages {
    hideNoRowsMessage: boolean
    noRowsMessage: string
}

export interface ITableWidgetSummaryRow {
    label: string
    aggregation: string
}

export interface ITableWidgetInteractions {
    crosssNavigation: ITableWidgetCrossNavigation
    link: ITableWidgetLinks
    preview: ITableWidgetPreview
    selection: ITableWidgetSelection
}

export interface ITableWidgetCrossNavigation {
    enabled: boolean
    type: string
    column: string
    icon?: string
    name: string
    parameters: ITableWidgetParameter[]
}

export interface ITableWidgetLinks {
    enabled: boolean
    links: ITableWidgetLink[]
}

export interface ITableWidgetLink {
    type: string
    icon?: string
    baseurl: string
    column?: string
    action: string
    parameters: ITableWidgetParameter[]
}

export interface ITableWidgetParameter {
    enabled: boolean
    name: string
    type: string
    value?: string
    column?: string
    driver?: string
    dataset?: string
    json?: string
}

export interface ITableWidgetPreview {
    enabled: boolean
    type: string
    parameters: ITableWidgetParameter[]
    dataset: number
    column?: string
    directDownload: boolean
    icon?: stirng
}

export interface ITableWidgetSelection {
    enabled: boolean
    modalColumn: string
    multiselection: {
        enabled: boolean
        properties: {
            'background-color': string
            color: string
        }
    }
}

export interface ITableWidgetPagination {
    enabled: boolean
    itemsNumber: number
}

export interface ITableWidgetStyle {
    borders: ITableWidgetBordersStyle
    columns: ITableWidgetColumnStyles
    columnGroups: ITableWidgetColumnStyles
    headers: ITawbleWidgetHeadersStyle
    padding: ITableWidgetPaddingStyle
    rows: ITableWidgetRowsStyle
    shadows: ITableWidgetShadowsStyle
    summary: ITableWidgetSummaryStyle
}

export interface ITableWidgetBordersStyle {
    enabled: boolean
    properties: {
        'border-bottom-left-radius': string
        'border-bottom-right-radius': string
        'border-style': string
        'border-top-left-radius': string
        'border-top-right-radius': string
        'border-width': string
        'border-color': string
    }
}

export interface ITableWidgetColumnStyles {
    enabled: boolean
    styles: ITableWidgetColumnStyle[]
}

export interface ITableWidgetColumnStyle {
    target: string | string[]
    properties: {
        width: string | number
        'background-color': string
        color: string
        'justify-content': string
        'font-size': string
        'font-family': string
        'font-style': string
        'font-weight': string
    }
}

export interface ITawbleWidgetHeadersStyle {
    height: number
    properties: {
        'background-color': string
        color: string
        'justify-content': string
        'font-size': string
        'font-family': string
        'font-style': string
        'font-weight': string
    }
}

export interface ITableWidgetPaddingStyle {
    enabled: boolean
    properties: {
        'padding-top': string
        'padding-left': string
        'padding-bottom': string
        'padding-right': string
        unlinked: boolean
    }
}

export interface ITableWidgetRowsStyle {
    height: number
    selectionColor: string
    multiselectable: boolean
    alternatedRows: {
        enabled: boolean
        evenBackgroundColor: string
        oddBackgroundColor: string
    }
}

export interface ITableWidgetShadowsStyle {
    enabled: boolean
    properties: {
        'box-shadow': string
        color: strubg
    }
}

export interface ITableWidgetSummaryStyle {
    'font-family': string
    'font-style': string
    'font-size': string
    'font-weight': string
    color: string
    'background-color': string
    'justify-content': string
}

export interface ITableWidgetTooltipStyle {
    target: string | string[]
    enabled: boolean
    prefix: string
    suffix: string
    precision: number
    header: {
        enabled: boolean
        text: string
    }
}

export interface ITableWidgetVisualization {
    visualizationTypes: ITableWidgetVisualizationTypes
    visibilityConditions: ITableWidgetVisibilityConditions
}

export interface ITableWidgetVisualizationTypes {
    enabled: boolean
    types: ITableWidgetVisualizationType[]
}

export interface ITableWidgetVisualizationType {
    target: string | string[]
    type: string
    precision?: number
    prefix?: string
    suffix?: string
    pinned?: string
    min?: number
    max?: number
    alignment?: string
    color?: string
    'background-color'?: string
}

export interface ITableWidgetVisibilityConditions {
    enabled: boolean
    conditions: ITableWidgetVisibilityCondition[]
}

export interface ITableWidgetVisibilityCondition {
    target: string[]
    hide: boolean
    hidePdf: boolean
    condition: {
        type: string
        variable?: string
        variableValue?: string
        operator?: string
        value?: string
    }
}

export interface ITableWidgetResponsive {
    xs: boolean
    sm: boolean
    md: boolean
    lg: boolean
    xl: boolean
}

export interface IWidgetColumn {
    id?: string
    columnName: string
    alias: string
    type: string
    fieldType: string
    multiValue: boolean
    aggregation: string
    filter: IWidgetColumnFilter
    formula?: string
    formulaEditor?: string
}

export interface IWidgetColumnFilter {
    enabled: boolean
    operator: string
    value: string
    value2?: string
}

export interface IWidgetEditorDataset {
    id: number
    label: string
    cache: boolean
    parameters?: any[]
    drivers?: any[]
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
    isNearRealtimeSupported: boolean
    cache?: boolean
    indexes?: any[]
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
    parameters: IModelDatasetParameter[]
    drivers: any[]
}

interface IModelDatasetParameter {
    multivalue: boolean
    name: string
    type: string
    value: string
}

export interface IIcon {
    category: string
    className: string
    fontFamily: string
    fontWeight: number
    id: number
    label: string
    unicode: string
    visible: booleam
}

export interface IWidgetStyleToolbarModel {
    'font-weight'?: string
    'font-style'?: string
    'font-size'?: string
    'font-family'?: string
    'justify-content'?: string
    color?: string
    'background-color'?: string
    icon?: string
}

export interface IVariable {
    name: string,
    type: string,
    value: string,
    dataset?: string,
    column?: string,
    attribute?: string,
    driver?: string
}