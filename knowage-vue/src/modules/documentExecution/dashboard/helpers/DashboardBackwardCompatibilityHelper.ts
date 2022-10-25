import { formatTableWidget } from './tableWidget/TableWidgetCompatibilityHelper'
import { formatSelectorWidget } from '@/modules/documentExecution/dashboard/helpers/selectorWidget/SelectorWidgetCompatibilityHelper'
import { IAssociation, IDashboardConfiguration, IDataset, IDatasetParameter, ISelection, IWidget, IWidgetColumn, IWidgetColumnFilter, IWidgetEditorDataset } from '../Dashboard'
import { formatSelectionWidget } from './selectionWidget/SelectionsWidgetCompatibilityHelper'
import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'

const datasetIdLabelMap = {}

export const formatModel = (model: any, document: any, datasets: IDataset[]) => {
    if (!model.sheets) return

    console.log(">>>>>>>>>>>>>>>>>>> LOADED MODEL: ", model)
    loadDatasetIdNameMap(datasets)
    const formattedModel = {
        id: cryptoRandomString({ length: 16, type: 'base64' }),
        widgets: [],
        version: model.knowageVersion,
        configuration: getFormattedModelConfiguration(model, document),
        sheets: []
    } as any
    for (let i = 0; i < model.sheets.length; i++) {
        const formattedSheet = formatSheet(model.sheets[i], formattedModel)
        formattedModel.sheets.push(formattedSheet)
    }

    console.log("FORMATTED MODEL: ", formattedModel)
    return formattedModel
}

const loadDatasetIdNameMap = (datasets: IDataset[]) => {
    if (!datasets) return
    datasets.forEach((dataset: IDataset) => {
        datasetIdLabelMap[dataset.label] = dataset.id.dsId
    })
}

const getDatasetId = (datasetLabel: string) => {
    return datasetIdLabelMap[datasetLabel]
}

const getFormattedModelConfiguration = (model: any, document: any) => {
    const formattedConfiguration = { id: document.id, name: document.name, label: document.label, description: document.description, associations: getFormattedAssociations(model), datasets: getFormattedDatasets(model), variables: getFormattedVariables(model), selections: getFormattedSelections(model), themes: {} } as IDashboardConfiguration

    return formattedConfiguration
}

const getFormattedAssociations = (model: any) => {
    if (!model.configuration || !model.configuration.associations) return []
    const formattedAssociations = [] as IAssociation[]
    for (let i = 0; i < model.configuration.associations.length; i++) {
        formattedAssociations.push(getFormattedAssociation(model.configuration.associations[i]))
    }
    return formattedAssociations
}

const getFormattedAssociation = (association: any) => {
    const formattedAssociation = { id: association.id, fields: [] } as IAssociation
    association.fields?.forEach((field: { column: string, store: string, type: string }) => formattedAssociation.fields.push({ column: field.column, dataset: getDatasetId(field.store) }))
    return formattedAssociation
}

const getFormattedDatasets = (model: any) => {
    if (!model.configuration || !model.configuration.datasets) return
    const formattedDatasets = [] as IWidgetEditorDataset[]
    for (let i = 0; i < model.configuration.datasets.length; i++) {
        formattedDatasets.push(getFormattedDataset(model.configuration.datasets[i]))
    }

    return formattedDatasets
}

const getFormattedDataset = (dataset: any) => {
    const formattedDataset = { id: dataset.dsId, dsLabel: dataset.dsLabel, cache: dataset.useCache } as IWidgetEditorDataset
    if (dataset.indexes) formattedDataset.indexes = dataset.indexes
    if (dataset.parameters) formattedDataset.parameters = getFormattedDatasetParameters(dataset)

    return formattedDataset
}

const getFormattedDatasetParameters = (dataset: any) => {
    const parameters = [] as IDatasetParameter[]
    Object.keys(dataset.parameters).forEach((key: string) => parameters.push({
        name: key,
        type: "static",
        value: dataset.parameters[key]
    }))
    return parameters
}

const getFormattedVariables = (model: any) => {
    const formattedVariables = [] as { name: string, type: string, value: string }[]
    if (!model.configuration || !model.configuration.variables) return formattedVariables
    for (let i = 0; i < model.configuration.variables.length; i++) {
        const tempVariable = model.configuration.variables[i]
        const formattedVariable = { name: tempVariable.name, type: tempVariable.type, value: '' }
        switch (formattedVariable.type) {
            case 'static':
                formattedVariable.value = tempVariable.value;
                break
            case 'dataset':
                formattedVariable.value = tempVariable.column;
                break
            case 'driver':
                formattedVariable.value = tempVariable.driver;
                break
            case 'profile':
                formattedVariable.value = tempVariable.attribute;
                break
        }
        formattedVariables.push(formattedVariable)
    }

    return formattedVariables
}

const getFormattedSelections = (model: any) => {
    if (!model.configuration || !model.selections) return []
    const formattedSelections = [] as ISelection[]
    model.selections.forEach((selection: { ds: string, columnName: string, value: string | (string | number)[], aggregated: boolean }) => {
        formattedSelections.push({ datasetId: getDatasetId(selection.ds), datasetLabel: selection.ds, columnName: selection.columnName, value: Array.isArray(selection.value) ? selection.value : [selection.value], aggregated: selection.aggregated, timestamp: new Date().getTime() })
    })
    return formattedSelections
}

const formatSheet = (sheet: any, formattedModel: any) => {
    if (!sheet.widgets) return

    const formattedSheet = deepcopy(sheet)
    formattedSheet.widgets = { lg: [] }

    for (let i = 0; i < sheet.widgets.length; i++) {
        const tempWidget = sheet.widgets[i]
        formattedSheet.widgets.lg.push({ id: tempWidget.id, h: tempWidget.sizeY, w: tempWidget.sizeX, x: tempWidget.col, y: tempWidget.row, i: cryptoRandomString({ length: 16, type: 'base64' }), moved: false })
        addWidgetToModel(tempWidget, formattedModel)
    }

    return formattedSheet
}

const addWidgetToModel = (widget: any, formattedModel: any) => {
    if (checkIfWidgetInModel(widget, formattedModel)) return
    formattedModel.widgets.push(formatWidget(widget))
}

const checkIfWidgetInModel = (widget: any, formattedModel: any) => {
    let found = false
    if (!formattedModel.widgets) return found
    for (let i = 0; i < formattedModel.widgets.length; i++) {
        if (formattedModel.widgets[i].id === widget.id) {
            found = true
            break
        }
    }

    return found
}

export const formatWidget = (widget: any) => {
    let formattedWidget = {} as any

    switch (widget.type) {
        case 'table':
            formattedWidget = formatTableWidget(widget)
            break;
        case 'selector':
            formattedWidget = formatSelectorWidget(widget)
            break;
        case 'selection':
            formattedWidget = formatSelectionWidget(widget)
    }

    return formattedWidget
}

export const getFiltersForColumns = (formattedWidget: IWidget, oldWidget: any) => {
    if (!oldWidget.filters || oldWidget.filters.length === 0) return
    for (let i = 0; i < oldWidget.filters.length; i++) {
        const tempFilter = oldWidget.filters[i]
        const index = formattedWidget.columns?.findIndex((column: IWidgetColumn) => column.columnName === tempFilter.colName)
        if (index !== -1) {
            formattedWidget.columns[index].filter = { enabled: true, operator: tempFilter.filterOperator, value: tempFilter.filterVal1 }
            if (tempFilter.filterVal2 && formattedWidget.columns[index].filter) (formattedWidget.columns[index].filter as IWidgetColumnFilter).value2 = tempFilter.filterVal2
        }
    }
}
