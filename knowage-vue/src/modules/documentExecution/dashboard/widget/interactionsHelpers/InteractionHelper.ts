import { IDashboard, IDataset, ISelection, IWidgetCrossNavigation, IWidgetInteractionParameter, } from "../../Dashboard"
import { ICrossNavigationParameter } from '@/modules/documentExecution/main/DocumentExecution'
import { getAssociativeSelections } from './DatasetAssociationsHelper'
import { emitter } from '../../DashboardHelpers'

interface IClickedValue { value: string, type: string }

export const loadAssociativeSelections = async (model: IDashboard, datasets: IDataset[], selections: ISelection[], $http: any) => {
    const tempResponse = await getAssociativeSelections(model, datasets, selections, $http)
    if (tempResponse) emitter.emit('associativeSelectionsLoaded', tempResponse)
}

export const updateStoreSelections = (newSelection: ISelection, currentActiveSelections: ISelection[], dashboardId: string, updateSelectionFunction: Function, $http: any) => {
    const index = currentActiveSelections.findIndex((activeSelection: ISelection) => activeSelection.datasetId === newSelection.datasetId && activeSelection.columnName === newSelection.columnName)
    index !== -1 ? currentActiveSelections[index] = newSelection : currentActiveSelections.push(newSelection)
    updateSelectionFunction(dashboardId, currentActiveSelections, $http)
}

export const executeCrossNavigation = (documentCrossNavigationOutputParameters: ICrossNavigationParameter[], crossNavigationId: number) => {
    console.log("TODO: executeCrossNavigation() - dynamicValue: ", documentCrossNavigationOutputParameters)
    console.log("TODO: executeCrossNavigation() - crossNavigationId: ", crossNavigationId)
    const payload = { documentCrossNavigationOutputParameters: documentCrossNavigationOutputParameters, crossNavigationId: crossNavigationId }
    emitter.emit('executeCrossNavigation', payload)
}



export const executeTableWidgetCrossNavigation = (clickedValue: IClickedValue, formattedRow: any, crossNavigationModel: IWidgetCrossNavigation) => {
    const outputParameters = getFormattedTableOutputParameters(clickedValue, formattedRow, crossNavigationModel)
    console.log(' ------------------------- >>>> outputParameters: ', outputParameters)
    // const payload = { dynamicValue: dynamicValue, crossNavigationId: crossNavigationId }
    // emitter.emit('executeCrossNavigation', payload)
}

const getFormattedTableOutputParameters = (clickedValue: IClickedValue, formattedRow: any, crossNavigationModel: IWidgetCrossNavigation) => {
    console.log("formattedRow ", formattedRow)
    console.log("crossNavigationModel ", crossNavigationModel)
    const formattedOutputParameters = [] as ICrossNavigationParameter[]
    crossNavigationModel.parameters.forEach((crossNavigationParameter: IWidgetInteractionParameter) => {
        switch (crossNavigationParameter.type) {
            case 'static':
                formattedOutputParameters.push(getFormattedFixedOutputParameter(crossNavigationParameter))
                break
            case 'dynamic':
                formattedOutputParameters.push(getFormattedDynamicOutputParameter(clickedValue, crossNavigationParameter, formattedRow))
                break
            case 'selection':
        }
    })
    return formattedOutputParameters
}

const getFormattedFixedOutputParameter = (crossNavigationParameter: IWidgetInteractionParameter) => {
    const value = crossNavigationParameter.value ?? ''
    return {
        targetDriverUrlName: '',
        parameterValue: [{ value: value, description: value }],
        multivalue: false,
        type: 'formSourceDocumentOutputParameter',
        parameterType: 'string'
    } as ICrossNavigationParameter
}

const getFormattedDynamicOutputParameter = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter, formattedRow: any) => {
    const valueAndType = getDynamicValueAndType(clickedValue, crossNavigationParameter, formattedRow)
    const value = valueAndType ? valueAndType.value : ''
    return {
        targetDriverUrlName: '',
        parameterValue: [{ value: value, description: value }],
        multivalue: false,
        type: 'formSourceDocumentOutputParameter',
        parameterType: valueAndType ? valueAndType.type : '' // TODO
    } as ICrossNavigationParameter
}

const getDynamicValueAndType = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter, formattedRow: any) => {
    // TODO - REFACTOR ?
    if (!crossNavigationParameter.column) return { value: ['date', 'timestamp'].includes(clickedValue.type) ? getFormattedDateValue(clickedValue.value) : clickedValue.value, type: clickedValue.type }
    console.log(' ------------------------- >>>> clickedValue: ', clickedValue)
    console.log(' ------------------------- >>>> crossNavigationParameter: ', crossNavigationParameter)
    console.log(' ------------------------- >>>> formattedRow: ', formattedRow)
    const rowField = formattedRow[crossNavigationParameter.column]
    if (!rowField) return null
    const value = ['date', 'timestamp'].includes(clickedValue.type) ? getFormattedDateValue(rowField.value) : rowField.value
    return { value: value, type: rowField.type }
}

const getFormattedDateValue = (valueAsString: string) => {
    console.log('------ valueAsString: ', valueAsString)
}

// const getFormattedSelectionOutputParameter = (crossNavigationParameter: IWidgetInteractionParameter) => {
//     return {}
// }



// export interface ICrossNavigationParameter {
//     targetDriverUrlName: string,
//     parameterValue: { value: string | number, description: string }[],
//     multivalue: boolean,
//     type: 'fixed' | 'fromSourceDocumentDriver' | 'formSourceDocumentOutputParameter',
//     parameterType?: string,
//     selectionType?: string
// }






// export const executeCrossNavigation = (dynamicValue: string, crossNavigation: IWidgetCrossNavigation) => {
//     console.log("TODO: executeCrossNavigation() - dynamicValue: ", dynamicValue, ', crossNavigation: ', crossNavigation)
//     emitter.emit('executeCrossNavigation',)
// }

export const executePreview = (datasetLabel: string) => {
    console.log("TODO: executePreview() - datasetLabel: ", datasetLabel)
}

