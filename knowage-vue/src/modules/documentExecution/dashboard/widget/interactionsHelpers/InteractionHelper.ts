import { IDashboard, IDataset, ISelection, IWidgetCrossNavigation, IWidgetInteractionParameter, } from "../../Dashboard"
import { ICrossNavigationParameter } from '@/modules/documentExecution/main/DocumentExecution'
import { getAssociativeSelections } from './DatasetAssociationsHelper'
import { emitter } from '../../DashboardHelpers'
import dashboardStore from '@/modules/documentExecution/dashboard/Dashboard.store'
import moment from "moment"

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

export const executeCrossNavigation = (documentCrossNavigationOutputParameters: ICrossNavigationParameter[], crossNavigationName: string | undefined) => {
    const payload = { documentCrossNavigationOutputParameters: documentCrossNavigationOutputParameters, crossNavigationName: crossNavigationName }
    emitter.emit('executeCrossNavigation', payload)
}



export const executeTableWidgetCrossNavigation = (clickedValue: IClickedValue, formattedRow: any, crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const outputParameters = getFormattedTableOutputParameters(clickedValue, formattedRow, crossNavigationModel, dashboardId)
    executeCrossNavigation(outputParameters, crossNavigationModel.name)
}

const getFormattedTableOutputParameters = (clickedValue: IClickedValue, formattedRow: any, crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const formattedOutputParameters = [] as ICrossNavigationParameter[]
    crossNavigationModel.parameters.forEach((crossNavigationParameter: IWidgetInteractionParameter) => {
        switch (crossNavigationParameter.type) {
            case 'static':
                formattedOutputParameters.push(getFormattedFixedOutputParameter(crossNavigationParameter))
                break
            case 'dynamic':
                formattedOutputParameters.push(getFormattedTableDynamicOutputParameter(clickedValue, crossNavigationParameter, formattedRow))
                break
            case 'selection':
                addSelectionTypeOutputParameter(crossNavigationParameter, formattedOutputParameters, dashboardId)
        }
    })
    return formattedOutputParameters
}

const getFormattedFixedOutputParameter = (crossNavigationParameter: IWidgetInteractionParameter) => {
    const value = crossNavigationParameter.value ?? ''
    const formattedValue = crossNavigationParameter.dataType === 'date' ? getFormattedDateValue(value, 'date') : value
    return {
        targetDriverUrlName: '',
        parameterValue: [{ value: formattedValue, description: formattedValue }],
        multivalue: false,
        type: 'fromSourceDocumentOutputParameter',
        parameterType: getDriverParameterTypeFromOutputParameterType(crossNavigationParameter.dataType),
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter
}

const getFormattedTableDynamicOutputParameter = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter, formattedRow: any) => {
    const valueAndType = getDynamicValueAndTypeForTableDynamicOutputParameter(clickedValue, crossNavigationParameter, formattedRow)
    const value = valueAndType ? valueAndType.value : ''
    return {
        targetDriverUrlName: '',
        parameterValue: [{ value: value, description: value }],
        multivalue: false,
        type: 'fromSourceDocumentOutputParameter',
        parameterType: getDriverParameterTypeFromOutputParameterType(crossNavigationParameter.dataType),
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter
}

const getDynamicValueAndTypeForTableDynamicOutputParameter = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter, formattedRow: any) => {
    // TODO - REFACTOR ?
    if (!crossNavigationParameter.column) {
        if (clickedValue.type === 'icon') return { value: '', type: 'string' }
        else return { value: ['date', 'timestamp'].includes(clickedValue.type) ? getFormattedDateValue(clickedValue.value, clickedValue.type) : clickedValue.value, type: clickedValue.type }
    }
    const rowField = formattedRow[crossNavigationParameter.column]
    if (!rowField) return null
    const fieldTypeIsDate = ['date', 'timestamp'].includes(rowField.type)
    const value = fieldTypeIsDate ? getFormattedDateValue(rowField.value, rowField.type) : rowField.value
    return { value: value, type: fieldTypeIsDate ? 'date' : 'string' } // TODO
}

const getFormattedDateValue = (valueAsString: string, type: string) => {
    const format = type === 'timestamp' ? 'DD/MM/YYYY HH:mm:ss.SSS' : 'DD/MM/YYYY'
    const date = moment(valueAsString, format)
    return date.isValid() ? date.valueOf() : ''
}

export const executeHTMLandTextWidgetCrossNavigation = (dynamicValue: string, crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const clickedValue = { value: dynamicValue, type: '' }
    const outputParameters = getFormattedHTMLandTextWidgetOutputParameters(clickedValue, crossNavigationModel, dashboardId)
    executeCrossNavigation(outputParameters, crossNavigationModel.name)
}


const getFormattedHTMLandTextWidgetOutputParameters = (clickedValue: IClickedValue, crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const formattedOutputParameters = [] as ICrossNavigationParameter[]
    crossNavigationModel.parameters.forEach((crossNavigationParameter: IWidgetInteractionParameter) => {
        switch (crossNavigationParameter.type) {
            case 'static':
                formattedOutputParameters.push(getFormattedFixedOutputParameter(crossNavigationParameter))
                break
            case 'dynamic':
                formattedOutputParameters.push(getFormattedHTMLandTextDynamicOutputParameter(clickedValue, crossNavigationParameter))
                break
            case 'selection':
                addSelectionTypeOutputParameter(crossNavigationParameter, formattedOutputParameters, dashboardId)
        }
    })
    return formattedOutputParameters
}

const getFormattedHTMLandTextDynamicOutputParameter = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter) => {
    const valueAndType = getDynamicValueAndTypeForHTMLandTextDynamicOutputParameter(clickedValue, crossNavigationParameter)
    const value = valueAndType ? valueAndType.value : ''
    return {
        targetDriverUrlName: '',
        parameterValue: [{ value: value, description: value }],
        multivalue: false,
        type: 'fromSourceDocumentOutputParameter',
        parameterType: getDriverParameterTypeFromOutputParameterType(crossNavigationParameter.dataType),
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter

}

const getDynamicValueAndTypeForHTMLandTextDynamicOutputParameter = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter) => {
    if (!clickedValue.value) return { value: '', type: crossNavigationParameter.dataType }
    const value = crossNavigationParameter.dataType === 'date' ? getFormattedDateValue(clickedValue.value, 'date') : clickedValue.value
    return { value: value, type: crossNavigationParameter.dataType }
}

export const executeHighchartsCrossNavigation = (outputParameters: IWidgetInteractionParameter[], crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {

    const formattedOutputParameters = getFormattedChartOutputParameters(outputParameters, crossNavigationModel, dashboardId)
    executeCrossNavigation(formattedOutputParameters, crossNavigationModel.name)
}

export const executeChartJSCrossNavigation = (outputParameters: IWidgetInteractionParameter[], crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    console.log('------- outputParameters: ', outputParameters)
    console.log('------- crossNavigationModel: ', crossNavigationModel)
    console.log('------- dashboardId: ', dashboardId)
    const formattedOutputParameters = getFormattedChartOutputParameters(outputParameters, crossNavigationModel, dashboardId)
    console.log('------- formattedOutputParameters: ', formattedOutputParameters)
    executeCrossNavigation(formattedOutputParameters, crossNavigationModel.name)
}

const getFormattedChartOutputParameters = (outputParameters: IWidgetInteractionParameter[], crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const formattedOutputParameters = [] as ICrossNavigationParameter[]
    crossNavigationModel.parameters.forEach((crossNavigationParameter: IWidgetInteractionParameter) => {
        switch (crossNavigationParameter.type) {
            case 'static':
                formattedOutputParameters.push(getFormattedFixedOutputParameter(crossNavigationParameter))
                break
            case 'dynamic':
                formattedOutputParameters.push(getFormattedChartDynamicOutputParameter(outputParameters, crossNavigationParameter))
                break
            case 'selection':
                addSelectionTypeOutputParameter(crossNavigationParameter, formattedOutputParameters, dashboardId)
        }
    })
    return formattedOutputParameters
}


const getFormattedChartDynamicOutputParameter = (outputParameters: IWidgetInteractionParameter[], crossNavigationParameter: IWidgetInteractionParameter) => {
    const index = outputParameters.findIndex((tempParameter: IWidgetInteractionParameter) => tempParameter.name === crossNavigationParameter.name)
    const outputParameter = index !== -1 ? outputParameters[index] : null
    const value = outputParameter?.value ?? ''
    return {
        targetDriverUrlName: '',
        parameterValue: [{ value: value, description: value }],
        multivalue: false,
        type: 'fromSourceDocumentOutputParameter',
        parameterType: getDriverParameterTypeFromOutputParameterType(crossNavigationParameter.dataType),
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter
}

const addSelectionTypeOutputParameter = (crossNavigationParameter: IWidgetInteractionParameter, formattedOutputParameters: ICrossNavigationParameter[], dashboardId: string) => {
    const tempParameter = getFormattedSelectionOutputParameter(crossNavigationParameter, dashboardId)
    if (tempParameter) formattedOutputParameters.push(tempParameter)
}

const getFormattedSelectionOutputParameter = (crossNavigationParameter: IWidgetInteractionParameter, dashboardId: string) => {
    const dashStore = dashboardStore()
    const activeSelections = dashStore.getSelections(dashboardId)
    const activeSelection = getActiveSelectionByDatasetAndColumn(crossNavigationParameter.dataset, crossNavigationParameter.column, activeSelections)

    if (!activeSelection) return null
    return {
        targetDriverUrlName: '',
        parameterValue: activeSelection.value.map((value: string | number) => { return { value: "" + value, description: "" + value } }), // TODO - see about DATE value
        multivalue: activeSelection.value.length > 1,
        type: 'fromSourceDocumentOutputParameter',
        parameterType: getDriverParameterTypeFromOutputParameterType(crossNavigationParameter.dataType),
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter
}

const getActiveSelectionByDatasetAndColumn = (datasetLabel: string | undefined, columnName: string | undefined, activeSelections: ISelection[]) => {
    if (!datasetLabel || !columnName) return null
    const index = activeSelections.findIndex((selection: ISelection) => selection.datasetLabel === datasetLabel && selection.columnName === columnName)
    return index !== -1 ? activeSelections[index] : null
}

const getDriverParameterTypeFromOutputParameterType = (outputParameterType: string) => {
    switch (outputParameterType) {
        case 'string':
            return 'STRING';
        case 'number':
            return 'NUM';
        case 'date':
            return 'DATE'
        default:
            return 'STRING'
    }
}

export const executePreview = (datasetLabel: string) => {
    console.log("TODO: executePreview() - datasetLabel: ", datasetLabel)
}

