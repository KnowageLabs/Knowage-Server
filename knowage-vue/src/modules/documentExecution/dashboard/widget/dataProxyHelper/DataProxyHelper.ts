import { AxiosResponse } from "axios"
import { IAssociation, IDashboard, IDataset, IModelDataset, IModelDatasetParameter, ISelection } from "../../Dashboard"

const datasetMapById = {}

export const getAssociativeSelections = async (model: IDashboard, datasets: IDataset[], $http: any) => {
    loadDatasetLabelIdMap(datasets)
    console.log(">>>>>>>>>>> MODEL: ", model)

    const tempDatasets = getDatasetsInfoFromModelDatasets(model.configuration.datasets, datasets)
    console.log(">>>>>>>>>>> MODEL DATASETS: ", model.configuration.datasets)
    console.log(">>>>>>>>>>> FULL DATASETS: ", tempDatasets)

    const postData = {
        associationGroup: getFormattedAssocitationsGroups(model.configuration.associations),
        selections: getFormattedSelections(model.configuration.selections),
        datasets: getFormattedModelDatasets(model.configuration.datasets),
        nearRealtime: getNearRealtimeDatasets(tempDatasets)
    }

    let tempResponse = null
    await $http
        .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/associativeSelections/`, postData)
        .then((response: AxiosResponse<any>) => (tempResponse = response.data))
        .catch(() => { })
    return tempResponse
}

const loadDatasetLabelIdMap = (datasets: IDataset[]) => {
    datasets.forEach((dataset: IDataset) => datasetMapById[dataset.id.dsId] = dataset)
}

const getDatasetsInfoFromModelDatasets = (modelDatasets: IModelDataset[], datasets: IDataset[]) => {
    const tempDatasets = [] as IDataset[]
    modelDatasets.forEach((modelDataset: IModelDataset) => {
        const tempDataset = findDatasetByLabel(modelDataset.dsLabel as string, datasets)
        if (tempDataset) {
            tempDatasets.push(tempDataset)
        }
    })
    return tempDatasets
}

const findDatasetByLabel = (datasetLabel: string, datasets: IDataset[]) => {
    if (!datasets) return null
    const index = datasets.findIndex((dataset: IDataset) => dataset.label === datasetLabel)
    return index !== -1 ? datasets[index] : null
}

const getDatasetById = (datasetId: number) => {
    return datasetMapById[datasetId]
}

const getFormattedAssocitationsGroups = (associations: IAssociation[]) => {
    const formattedAssociations = [] as any[]
    const datasetsUsedInAssociations = [] as string[]
    associations.forEach((association: IAssociation) => formattedAssociations.push({
        id: association.id,
        fields: getFormattedAssociationFields(association.fields, datasetsUsedInAssociations)
    }))
    return { datasets: datasetsUsedInAssociations, associations: formattedAssociations }
}

const getFormattedAssociationFields = (associationFields: { column: string, dataset: number }[], datasetsUsedInAssociations: string[]) => {
    return associationFields.map((field: { column: string, dataset: number }) => {
        const tempDataset = getDatasetById(field.dataset)
        if (tempDataset) {
            const index = datasetsUsedInAssociations.findIndex((dasetLabel: string) => dasetLabel === tempDataset.label)
            if (index === -1) datasetsUsedInAssociations.push(tempDataset.label)
        }
        return { column: field.column, store: tempDataset ? tempDataset.label : '' }
    })
}

const getFormattedSelections = (modelSelections: ISelection[]) => {
    const formattedSelctions = {}
    modelSelections.forEach((selection: ISelection) => {
        const key = selection.datasetLabel + '.' + selection.columnName
        formattedSelctions[key] = selection.value
    })
    return formattedSelctions
}

const getFormattedModelDatasets = (modelDatasets: IModelDataset[]) => {
    const formattedDatasets = {}
    modelDatasets.forEach((dataset: IModelDataset) => formattedDatasets[dataset.dsLabel as string] = getFormattedDatasetParameters(dataset))
    return formattedDatasets
}

const getFormattedDatasetParameters = (dataset: IModelDataset) => {
    const formattedParameters = {}
    if (dataset.parameters) dataset.parameters.forEach((parameter: IModelDatasetParameter) => formattedParameters[parameter.name] = parameter.value)
    return formattedParameters
}

const getNearRealtimeDatasets = (tempDatasets: IDataset[]) => {
    return tempDatasets.filter((dataset: IDataset) => dataset.isNearRealtimeSupported).map((dataset: IDataset) => dataset.label)
}