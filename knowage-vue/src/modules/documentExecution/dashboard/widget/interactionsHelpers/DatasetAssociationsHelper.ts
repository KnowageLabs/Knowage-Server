import { IAssociation, IAssociationField, IDashboard, IDataset, IDashboardDataset, IDashboardDatasetParameter, ISelection, IWidget, IWidgetColumn, IWidgetColumnFilter } from "../../Dashboard"
import { AxiosResponse } from "axios"
import { emitter } from '../../DashboardHelpers'
import { clearDatasetInterval } from "../../helpers/datasetRefresh/DatasetRefreshHelpers"

interface IFormattedFilter {
    filterOperator: string,
    filterVals: string[
    ],
    dataset: {
        dsId: number,
        label: string
    },
    colName: string
}

let datasetMapById = {}

export const getAssociativeSelections = async (model: IDashboard, datasets: IDataset[], selections: ISelection[], $http: any) => {
    emitter.emit('setWidgetLoading', true)
    loadDatasetLabelIdMap(datasets)

    const tempDatasets = getDatasetsInfoFromModelDatasets(model.configuration.datasets, datasets)
    const formattedAssocitationsGroups = getFormattedAssocitationsGroups(model.configuration.associations)

    formattedAssocitationsGroups.datasets?.forEach((datasetLabel: string) => {
        const datasetId = getDatasetIdByLabel(datasetLabel)
        if (datasetId) clearDatasetInterval(+datasetId)
    })

    const postData = {
        associationGroup: getFormattedAssocitationsGroups(model.configuration.associations),
        selections: getFormattedSelections(selections),
        datasets: getFormattedModelDatasets(model.configuration.datasets),
        nearRealtime: getNearRealtimeDatasets(tempDatasets)
    } as any

    const filters = getFiltersFromWidgets(model, postData.datasets)
    if (filters.length > 0) {
        postData.filters = filters
    }

    let tempResponse = null
    await $http
        .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/associativeSelections/`, postData)
        .then((response: AxiosResponse<any>) => (tempResponse = response.data))
        .catch(() => { })
    emitter.emit('setWidgetLoading', false)
    return tempResponse
}



const loadDatasetLabelIdMap = (datasets: IDataset[]) => {
    datasets.forEach((dataset: IDataset) => datasetMapById[dataset.id.dsId] = dataset)
}

const getDatasetsInfoFromModelDatasets = (modelDatasets: IDashboardDataset[], datasets: IDataset[]) => {
    const tempDatasets = [] as IDataset[]
    modelDatasets.forEach((modelDataset: IDashboardDataset) => {
        const tempDataset = findDatasetByLabel(modelDataset.dsLabel as string, datasets)
        if (tempDataset) {
            tempDatasets.push(tempDataset)
        }
    })
    return tempDatasets
}

const getDatasetIdByLabel = (datasetLabel: string) => {
    return Object.keys(datasetMapById).find(key => datasetMapById[key].label === datasetLabel);
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
        fields: getFormattedAssociationFields(association.fields, datasetsUsedInAssociations),
        description: getAssociationDescription(association.fields)
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
        return { column: field.column, store: tempDataset ? tempDataset.label : '', type: 'dataset' }
    })
}

const getAssociationDescription = (associationFields: { column: string, dataset: number }[]) => {
    let description = ''
    for (let i = 0; i < associationFields.length; i++) {
        description += getDatasetById(associationFields[i].dataset)?.label + '.' + associationFields[i].column
        if (i !== associationFields.length - 1) description += '='
    }
    return description
}

const getFormattedSelections = (modelSelections: ISelection[]) => {
    const formattedSelctions = {}
    modelSelections.forEach((selection: ISelection) => {
        const key = selection.datasetLabel + '.' + selection.columnName
        formattedSelctions[key] = selection.value
    })
    return formattedSelctions
}

const getFormattedModelDatasets = (modelDatasets: IDashboardDataset[]) => {
    const formattedDatasets = {}
    modelDatasets.forEach((dataset: IDashboardDataset) => formattedDatasets[dataset.dsLabel as string] = getFormattedDatasetParameters(dataset))
    return formattedDatasets
}

const getFormattedDatasetParameters = (dataset: IDashboardDataset) => {
    const formattedParameters = {}
    if (dataset.parameters) dataset.parameters.forEach((parameter: IDashboardDatasetParameter) => formattedParameters[parameter.name] = parameter.value)
    return formattedParameters
}

const getNearRealtimeDatasets = (tempDatasets: IDataset[]) => {
    return tempDatasets.filter((dataset: IDataset) => dataset.isNearRealtimeSupported).map((dataset: IDataset) => dataset.label)
}

const getFiltersFromWidgets = (model: IDashboard, datasetsInAssociation: any) => {
    const datasetLabels = datasetsInAssociation ? Object.keys(datasetsInAssociation) : []
    const filters = [] as IFormattedFilter[]
    model.widgets.forEach((widget: IWidget) => {
        const datasetLabel = widget.dataset ? getDatasetById(widget.dataset)?.label : ''
        if (datasetLabels.includes(datasetLabel)) {
            widget.columns?.forEach((column: IWidgetColumn) => {
                if (column.filter.enabled) {
                    const formattedFilter = {
                        filterOperator: column.filter.operator,
                        filterVals: [`('${column.filter.value}')`],
                        dataset: {
                            dsId: widget.dataset,
                            label: datasetLabel
                        },
                        colName: column.columnName
                    } as IFormattedFilter
                    filters.push(formattedFilter)
                }
            })
        }
    })
    return filters
}


export const selectionsUseDatasetWithAssociation = (selections: ISelection[], associations: IAssociation[]) => {
    if (!selections || !associations) return false
    for (let i = 0; i < selections.length; i++) {
        for (let j = 0; j < associations.length; j++) {
            const index = associations[j].fields?.findIndex((field: IAssociationField) => field.dataset === selections[i].datasetId)
            if (index !== -1) {
                return true
            }
        }
    }

    return false
}

export const datasetIsUsedInAssociations = (datasetId: number, associations: IAssociation[]) => {
    for (let i = 0; i < associations.length; i++) {
        for (let j = 0; j < associations[i].fields.length; j++) {
            const field = associations[i].fields[j]
            if (field.dataset === datasetId) return true
        }
    }
    return false
}