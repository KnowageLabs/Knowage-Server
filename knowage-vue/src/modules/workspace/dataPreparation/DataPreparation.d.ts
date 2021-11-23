export interface ITransformation {
    type: string
    parameters: Array<Array<ITransformationParameter>>
    description: string
    name: string
}

export interface ITransformationParameter {
    name: string
    value: string
    availableOptions?: Array
    type: string
}

export interface IDataPreparationColumn {
    header: string
    type: string
    disabled: Boolean
}

export interface IDataPreparationDataset {
    name: string
    label: string
    description: string
    visibility: string
    refreshRate: {}
}
