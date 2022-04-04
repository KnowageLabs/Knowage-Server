export interface iDimension {
    DIMENSION_NM: string,
    DIMENSION_PREFIX: string,
    DIMENSION_DS: string
}

export interface iDimensionMetadata {
    DIM_FIELDS: iDimensionField[],
    MATCH_LEAF_FIELDS: any
}

export interface iDimensionField {
    ID: string,
    NAME: string,
    VISIBLE: boolean,
    EDITABLE: boolean,
    PARENT: boolean,
    TYPE: string
}

export interface iNodeMetadata {
    CONFIGS: iNodeMetadataConfigurations
    GENERAL_FIELDS: iNodeMetadataField[],
    NODE_FIELDS: iNodeMetadataField[],
    LEAF_FIELDS: iNodeMetadataField[],
}

export interface iNodeMetadataConfigurations {
    NUM_LEVELS: string,
    ALLOW_DUPLICATE: string,
    TREE_LEAF_CD: string,
    NODE: string,
    FILL_EMPTY: string,
    ORIG_NODE: string,
    TREE_NODE_CD: string,
    DIMENSION_ID: string,
    TREE_NODE_NM: string,
    FILL_VALUE: string,
    DIMENSION_NM: string,
    TREE_LEAF_NM: string,
    DIMENSION_CD: string,
    LEAF: string,
    TREE_LEAF_ID: string
}

export interface iNodeMetadataField {
    ID: string,
    NAME: string,
    VISIBLE: boolean,
    EDITABLE: boolean,
    PARENT: boolean,
    TYPE: string,
    SINGLE_VALUE: boolean,
    REQUIRED: boolean,
    ORDER_FIELD: boolean,
    FIX_VALUE: string,
    value?: string | number
}

export interface iDimensionFilter {
    NAME: string,
    TYPE: string,
    DEFAULT: string,
    CONDITION1?: string,
    VALUE?: string | number | Date
}

export interface iHierarchy {
    HIER_CD: string,
    HIER_NM: string,
    HIER_TP: string,
    HIER_DS: string
}

export interface iNode {
    key: number | string,
    icon: string,
    id: number,
    label: string,
    children: iNode[],
    data: string | any,
    parentKey?: string
    parent?: iNode
}

export interface iHierarchyTarget {
    DIMENSION: string,
    GENERAL_INFO_T: string,
    HIER_CD_M: string,
    HIER_CD_T: string,
    HIER_NM_M: string,
    HIER_NM_T: string,
    MT_ID: number,
    NODE_CD_M: string,
    NODE_CD_T: string,
    NODE_LEV_M: number,
    NODE_LEV_T: number,
    NODE_NM_M: string,
    NODE_NM_T: string,
    PATH_CD_T: string,
    PATH_NM_T: string,
}