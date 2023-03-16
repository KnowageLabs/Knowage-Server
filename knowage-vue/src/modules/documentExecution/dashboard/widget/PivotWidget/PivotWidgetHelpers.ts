import { IDataset, ISelection, IWidget, IWidgetCrossNavigation, IWidgetInteractionParameter } from "../../Dashboard"

interface ISelectionValue { columnName: string, value: string, columnType: string }

export const getFormattedClickedValueForCrossNavigation = (cellEvent: any, dataFields: any, crossNavigationOptions: IWidgetCrossNavigation) => {
    if (!cellEvent || cellEvent.area !== 'data') return null
    const selectionValues = [] as ISelectionValue[]
    addSelectionValues(cellEvent.cell.columnPath, cellEvent.columnFields, selectionValues)
    addSelectionValues(cellEvent.cell.rowPath, cellEvent.rowFields, selectionValues)
    const formattedOutputParameters = getFormattedOutputParameters(selectionValues, crossNavigationOptions.parameters)
    return formattedOutputParameters
}

const getFormattedOutputParameters = (selectionValues: ISelectionValue[], outputParameters: IWidgetInteractionParameter[]) => {
    const formattedOutputParameters = [] as IWidgetInteractionParameter[]
    outputParameters.forEach((outputParameter: IWidgetInteractionParameter) => {
        if (outputParameter.type === 'dynamic') {
            const formattedOutputParameter = getFormattedDynamicOutputParameter(selectionValues, outputParameter)
            if (formattedOutputParameter.value) formattedOutputParameters.push(getFormattedDynamicOutputParameter(selectionValues, outputParameter))
        } else {
            formattedOutputParameters.push(outputParameter)
        }
    })
    return formattedOutputParameters
}

const getFormattedDynamicOutputParameter = (selectionValues: ISelectionValue[], outputParameter: IWidgetInteractionParameter) => {
    const index = selectionValues.findIndex((selectionValue: ISelectionValue) => selectionValue.columnName === outputParameter.column)
    outputParameter.value = index !== -1 ? selectionValues[index].value : ''
    return outputParameter
}


export const createPivotTableSelection = (cellEvent: any, widgetModel: IWidget, datasets: IDataset[]) => {
    if (!cellEvent || cellEvent.area !== 'data') return null
    const selectionValues = [] as ISelectionValue[]
    addSelectionValues(cellEvent.cell.columnPath, cellEvent.columnFields, selectionValues)
    addSelectionValues(cellEvent.cell.rowPath, cellEvent.rowFields, selectionValues)
    return selectionValues.length > 0 ? createSelectionsFromSelectedValues(selectionValues, widgetModel, datasets) : null
}

const addSelectionValues = (path: string[], columnFields: any[], selectionValues: ISelectionValue[]) => {
    path.forEach((pathValue: string, index) => {
        if (columnFields[index]) {
            const column = columnFields[index]
            selectionValues.push({ columnName: column.caption, value: pathValue, columnType: column.dataType })
        }
    })
}

const createSelectionsFromSelectedValues = (selectionValues: ISelectionValue[], widgetModel: IWidget, datasets: IDataset[]) => {
    const selections = [] as ISelection[]
    selectionValues.forEach((selectionValue: ISelectionValue) => selections.push(createSelection([selectionValue.value], selectionValue.columnName, widgetModel, datasets)))
    return selections
}

const createSelection = (value: (string | number)[], columnName: string, widget: IWidget, datasets: IDataset[]) => {
    return { datasetId: widget.dataset as number, datasetLabel: getDatasetLabel(widget.dataset as number, datasets) as string, columnName: columnName, value: value, aggregated: false, timestamp: new Date().getTime() }
}

const getDatasetLabel = (datasetId: number, datasets: IDataset[]) => {
    const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
    return index !== -1 ? datasets[index].label : ''
}