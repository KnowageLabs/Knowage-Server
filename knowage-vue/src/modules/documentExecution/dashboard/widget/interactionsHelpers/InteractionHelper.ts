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


//#region ===== TABLE ======
export const executeTableWidgetCrossNavigation = (clickedValue: IClickedValue, formattedRow: any, crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const outputParameters = getFormattedTableOutputParameters(clickedValue, formattedRow, crossNavigationModel, dashboardId)
    executeCrossNavigation(outputParameters, crossNavigationModel.name)
}

const getFormattedTableOutputParameters = (clickedValue: IClickedValue, formattedRow: any, crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const formattedOutputParameters = [] as ICrossNavigationParameter[]
    for (let i = 0; i < crossNavigationModel.parameters.length; i++) {
        const crossNavigationParameter = crossNavigationModel.parameters[i] as IWidgetInteractionParameter
        if (!crossNavigationParameter.enabled) continue
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
    }
    return formattedOutputParameters
}

const getFormattedFixedOutputParameter = (crossNavigationParameter: IWidgetInteractionParameter) => {
    const value = crossNavigationParameter.value ?? ''
    const formattedValue = getFormattedFixedOutputValue(value, crossNavigationParameter.dataType)
    return {
        targetDriverUrlName: '',
        parameterValue: [{ value: formattedValue, description: formattedValue }],
        multivalue: false,
        type: 'fromSourceDocumentOutputParameter',
        parameterType: getDriverParameterTypeFromOutputParameterType(crossNavigationParameter.dataType),
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter
}

const getFormattedFixedOutputValue = (value: string, dataType: string) => {
    switch (dataType) {
        case 'date':
            return getFormattedDateValue(value, 'date')
        case 'number':
            return isNaN(value as any) ? null : +value
        default:
            return value
    }
}

const getFormattedTableDynamicOutputParameter = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter, formattedRow: any) => {
    const valueAndType = getDynamicValueAndTypeForTableDynamicOutputParameter(clickedValue, crossNavigationParameter, formattedRow)
    const value = valueAndType ? valueAndType.value : ''
    return {
        targetDriverUrlName: '',
        parameterValue: [{ value: value, description: value }],
        multivalue: false,
        type: 'fromSourceDocumentOutputParameter',
        parameterType: getDriverParameterTypeFromOutputParameterType(valueAndType?.type),
        outputDriverName: crossNavigationParameter.name
    } as ICrossNavigationParameter
}

const getDynamicValueAndTypeForTableDynamicOutputParameter = (clickedValue: IClickedValue, crossNavigationParameter: IWidgetInteractionParameter, formattedRow: any) => {
    if (!crossNavigationParameter.column) {
        if (clickedValue.type === 'icon') return { value: '', type: 'string' }
        else return { value: ['date', 'timestamp'].includes(clickedValue.type) ? getFormattedDateValue(clickedValue.value, clickedValue.type) : clickedValue.value, type: clickedValue.type }
    }
    const rowField = formattedRow[crossNavigationParameter.column]
    if (!rowField) return null
    const fieldTypeIsDate = ['date', 'timestamp'].includes(rowField.type)
    const value = fieldTypeIsDate ? getFormattedDateValue(rowField.value, rowField.type) : rowField.value
    return { value: value, type: rowField.type }
}

//#endregion ===== TABLE ======

const getFormattedDateValue = (valueAsString: string, type: string) => {
    const format = type === 'timestamp' ? 'DD/MM/YYYY HH:mm:ss.SSS' : 'DD/MM/YYYY'
    const date = moment(valueAsString, format)
    return date.isValid() ? date.valueOf() : ''
}

//#region ===== HTML/TEXT ======
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

//#endregion ===== HTML/TEXT ======

//#region ===== CHART ======
export const executeChartCrossNavigation = (outputParameters: IWidgetInteractionParameter[], crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const formattedOutputParameters = getFormattedChartOutputParameters(outputParameters, crossNavigationModel, dashboardId)
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

//#endregion ===== CHART ======

//#region ===== IMAGE ======
export const executeImageWidgetCrossNavigation = (crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const outputParameters = getFormattedImageWidgetOutputParameters(crossNavigationModel, dashboardId)
    executeCrossNavigation(outputParameters, crossNavigationModel.name)
}

const getFormattedImageWidgetOutputParameters = (crossNavigationModel: IWidgetCrossNavigation, dashboardId: string) => {
    const formattedOutputParameters = [] as ICrossNavigationParameter[]
    crossNavigationModel.parameters.forEach((crossNavigationParameter: IWidgetInteractionParameter) => {
        switch (crossNavigationParameter.type) {
            case 'static':
                formattedOutputParameters.push(getFormattedFixedOutputParameter(crossNavigationParameter))
                break
            case 'selection':
                addSelectionTypeOutputParameter(crossNavigationParameter, formattedOutputParameters, dashboardId)
        }
    })
    return formattedOutputParameters
}
//#endregion ===== IMAGE ======


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

const getDriverParameterTypeFromOutputParameterType = (outputParameterType: string | undefined) => {
    switch (outputParameterType) {
        case 'string':
        case 'text':
            return 'STRING';
        case 'number':
        case 'int':
        case 'float':
            return 'NUM';
        case 'date':
        case 'timestamp':
            return 'DATE'
        default:
            return 'STRING'
    }
}

export const executePreview = (datasetLabel: string) => {
    console.log("TODO: executePreview() - datasetLabel: ", datasetLabel)
}

