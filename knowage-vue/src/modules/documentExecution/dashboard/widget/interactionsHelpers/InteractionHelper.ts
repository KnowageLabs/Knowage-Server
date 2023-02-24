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
                formattedOutputParameters.push(getFormattedDynamicOutputParameter(clickedValue, crossNavigationParameter, formattedRow))
                break
            case 'selection':
                addSelectionTypeOutputParameter(crossNavigationParameter, formattedOutputParameters, dashboardId)
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
        type: 'fromSourceDocumentOutputParameter',
        parameterType: 'string',
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter
}

const getFormattedDynamicOutputParameter = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter, formattedRow: any) => {
    const valueAndType = getDynamicValueAndType(clickedValue, crossNavigationParameter, formattedRow)
    const value = valueAndType ? valueAndType.value : ''
    return {
        targetDriverUrlName: '',
        parameterValue: [{ value: value, description: value }],
        multivalue: false,
        type: 'fromSourceDocumentOutputParameter',
        parameterType: valueAndType ? valueAndType.type : '', // TODO
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter
}

const getDynamicValueAndType = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter, formattedRow: any) => {
    // TODO - REFACTOR ?
    if (!crossNavigationParameter.column) {
        if (clickedValue.type === 'icon') return { value: '', type: 'string' }
        else return { value: ['date', 'timestamp'].includes(clickedValue.type) ? getFormattedDateValue(clickedValue.value, clickedValue.type) : clickedValue.value, type: clickedValue.type }
    }
    const rowField = formattedRow[crossNavigationParameter.column]
    if (!rowField) return null
    const fieldTypeIsDate = ['date', 'timestamp'].includes(rowField.type)
    const value = fieldTypeIsDate ? getFormattedDateValue(rowField.value, rowField.type) : rowField.value
    return { value: value, type: fieldTypeIsDate ? 'DATE' : 'string' } // TODO
}

const getFormattedDateValue = (valueAsString: string, type: string) => {
    const format = type === 'timestamp' ? 'DD/MM/YYYY HH:mm:ss.SSS' : 'DD/MM/YYYY'
    const date = moment(valueAsString, format)
    return date.isValid() ? date.valueOf() : ''
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
        parameterType: 'string', // TODO - for date ?
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter
}

const getActiveSelectionByDatasetAndColumn = (datasetLabel: string | undefined, columnName: string | undefined, activeSelections: ISelection[]) => {
    if (!datasetLabel || !columnName) return null
    const index = activeSelections.findIndex((selection: ISelection) => selection.datasetLabel === datasetLabel && selection.columnName === columnName)
    return index !== -1 ? activeSelections[index] : null
}


export const executePreview = (datasetLabel: string) => {
    console.log("TODO: executePreview() - datasetLabel: ", datasetLabel)
}

