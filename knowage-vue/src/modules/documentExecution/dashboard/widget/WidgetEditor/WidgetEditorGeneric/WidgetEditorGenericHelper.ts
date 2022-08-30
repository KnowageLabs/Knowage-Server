import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"

export function getModelProperty(model: IWidget, propertyPath: string, action: string, newValue: any) {
    if (!model) return
    const stack = propertyPath?.split('.')
    if (!stack || stack.length === 0) return

    let property = null as any
    let tempModel = model
    while (stack.length > 1) {
        property = stack.shift()
        if (property && model) tempModel = tempModel[property]
        if (!tempModel) return
    }
    property = stack.shift()
    if (action === 'updateValue' && tempModel) tempModel[property] = newValue
    else if (action === 'getValue' && tempModel)
        return tempModel[property]

}