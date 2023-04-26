export interface iDriver {
    id?: number
    label?: string
    name?: string
    description?: string
    type?: string
    typeId?: number
    length?: number
    mask?: string
    modality?: string
    modalityValue?: any
    modalityValueForDefault?: any
    modalityValueForMax?: any
    defaultFormula?: string
    valueSelection?: any
    selectedLayer?: any
    selectedLayerProp?: any
    checks?: any
    functional?: boolean
    temporal?: boolean
}
