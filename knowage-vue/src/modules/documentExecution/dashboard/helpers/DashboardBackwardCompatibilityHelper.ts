import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'
import { formatTableWidget } from './tableWidget/TableWidgetCompatibilityHelper'
import { IDatasetParameter, IWidgetEditorDataset } from '../Dashboard'

export const formatModel = (model: any) => {
    if (!model.sheets) return

    // TODO - id
    const formattedModel = {
        id: 1,
        widgets: [],
        version: model.knowageVersion,
        configuration: getFormattedModelConfiguration(model),
        sheets: []
    } as any
    for (let i = 0; i < model.sheets.length; i++) {
        const formattedSheet = formatSheet(model.sheets[i], formattedModel)
        formattedModel.sheets.push(formattedSheet)
    }
    return formattedModel
}

const getFormattedModelConfiguration = (model: any) => {
    // TODO - id, name, label, description
    const formattedConfiguration = { id: '', name: '', label: '', description: '', associations: [], datasets: getFormattedDatasets(model), variables: getFormattedVariables(model), themes: {} }

    return formattedConfiguration
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
    const formattedDataset = { id: dataset.dsId, cache: dataset.useCache } as IWidgetEditorDataset
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

const formatSheet = (sheet: any, formattedModel: any) => {
    if (!sheet.widgets) return

    const formattedSheet = deepcopy(sheet)
    formattedSheet.widgets = { lg: [] }

    for (let i = 0; i < sheet.widgets.length; i++) {
        const tempWidget = sheet.widgets[i]
        formattedSheet.widgets.lg.push({ id: tempWidget.id, h: 5, w: 10, x: 0, y: 0, i: cryptoRandomString({ length: 16, type: 'base64' }), moved: false })
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
    }

    return formattedWidget
}
