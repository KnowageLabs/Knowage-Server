export interface ITransformation {
    type: string
    parameters: Array<ITransformationParameter>
    description: string
    name: string
}

export interface ITransformationParameter {
    name: string
    value?: string
    availableOptions?: Array
    type: string
    relatedToField?: string
    relatedToOption?: string
}

export interface IDataPreparationColumn {
    header: string
    Type: string
    fieldType: string
    fieldAlias: string
    disabled: boolean
}

export interface IDataPreparationDataset {
    name: string
    label: string
    description: string
    visibility: string
    refreshRate: {}
}
