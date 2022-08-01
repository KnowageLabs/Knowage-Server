import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"

export function getModelProperty(model: IWidget, propertyPath: string, action: string, newValue: any) {
    console.log(' >>> getModelProperty - model: ', model)
    console.log(' >>> getModelProperty - propertyPath: ', propertyPath)
    console.log(' >>> getModelProperty - action: ', action)
    console.log(' >>> getModelProperty - newValue: ', newValue)
    if (!model) return
    const stack = propertyPath?.split('.')
    if (!stack || stack.length === 0) return

    let property = null as any
    let tempModel = model
    while (stack.length > 1) {
        property = stack.shift()
        if (property && model) tempModel = tempModel[property]
    }
    property = stack.shift()
    if (action === 'updateValue') tempModel[property] = newValue
    else if (action === 'getValue'){ 
        console.log(">>> TEMP MODEL: ", tempModel)
        return tempModel[property]
    }
}