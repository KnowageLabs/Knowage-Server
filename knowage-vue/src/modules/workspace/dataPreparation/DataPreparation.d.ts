export interface ITransformation<T> {
    type: string
    parameters: Array<T>
    description: string
    name: string
}

export interface ITransformationParameter {
    name: string
    value?: Object
    availableOptions?: Array<any>
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
    id? : number
    name: string
    label?: string
    description: string
    dataSource: string
    visibility: string
    refreshRate: {}
}

export interface IFilterTransformationParameter {
    column: IDataPreparationColumn
    condition: string
    text?: string
    startDate?: Date
    endDate?: Date
}

export interface ISplitTransformationParameter {
    column: IDataPreparationColumn
    condition: string
    numOfChars?: number
    separator?: string
    outputColumn1: string
    outputType1: string
    outputColumn2: string
    outputType2: string
}
