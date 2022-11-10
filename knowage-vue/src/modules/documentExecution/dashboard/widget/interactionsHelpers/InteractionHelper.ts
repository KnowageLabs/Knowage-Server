import { IDashboard, IDataset, ISelection, IWidgetCrossNavigation } from "../../Dashboard"
import { getAssociativeSelections } from './DatasetAssociationsHelper'
import { emitter } from '../../DashboardHelpers'


export const loadAssociativeSelections = async (model: IDashboard, datasets: IDataset[], selections: ISelection[], $http: any) => {
    const tempResponse = await getAssociativeSelections(model, datasets, selections, $http)
    if (tempResponse) emitter.emit('associativeSelectionsLoaded', tempResponse)
}

export const updateStoreSelections = (newSelection: ISelection, currentActiveSelections: ISelection[], dashboardId: string, updateSelectionFunction: Function, $http: any) => {
    const index = currentActiveSelections.findIndex((activeSelection: ISelection) => activeSelection.datasetId === newSelection.datasetId && activeSelection.columnName === newSelection.columnName)
    index !== -1 ? currentActiveSelections[index] = newSelection : currentActiveSelections.push(newSelection)
    updateSelectionFunction(dashboardId, currentActiveSelections, $http)
}

export const executeCrossNavigation = (dynamicValue: string, crossNavigation: IWidgetCrossNavigation) => {
    console.log("TODO: executeCrossNavigation() - dynamicValue: ", dynamicValue, ', crossNavigation: ', crossNavigation)
}

export const executePreview = (datasetLabel: string) => {
    console.log("TODO: executePreview() - datasetLabel: ", datasetLabel)
}