import { IWidgetCrossNavigation, IWidgetInteractionParameter } from "../../Dashboard"

export const formatForCrossNavigation = (columnValue: string | number, crossNavigationOptions: IWidgetCrossNavigation) => {
    const formattedOutputParameters = getFormattedOutputParameters(columnValue, crossNavigationOptions.parameters)
    return formattedOutputParameters

}

const getFormattedOutputParameters = (columnValue: string | number, outputParameters: IWidgetInteractionParameter[]) => {
    const formattedOutputParameters = [] as IWidgetInteractionParameter[]
    outputParameters.forEach((outputParameter: IWidgetInteractionParameter) => {
        if (outputParameter.type === 'dynamic') {
            formattedOutputParameters.push({ ...outputParameter, value: '' + columnValue })
        } else {
            formattedOutputParameters.push(outputParameter)
        }
    })
    return formattedOutputParameters
}