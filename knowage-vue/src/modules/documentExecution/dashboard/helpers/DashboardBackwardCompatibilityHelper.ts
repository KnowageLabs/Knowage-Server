import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'
import { formatTableWidget } from './TableWidgetCompatibilityHelper'
import { IWidgetEditorDataset } from '../Dashboard'

export const formatModel = (model: any) => {
    console.log('FORMAT MODEL CALLED WITH: ', model)
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
    // TODO - What is id used for?
    const formattedConfiguration = { id: '', name: '', label: '', description: '', associations: [], datasets: getFormattedDatasets(model), variables: [], themes: {} }

    return formattedConfiguration
}

const getFormattedDatasets = (model: any) => {
    console.log('ORIGINAL DATASETS: ', model.configuration.datasets)
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

// TODO
const getFormattedDatasetParameters = (dataset: any) => {
    return []
}

const formatSheet = (sheet: any, formattedModel: any) => {
    if (!sheet.widgets) return

    const formattedSheet = deepcopy(sheet)
    formattedSheet.widgets = { lg: [] }

    for (let i = 0; i < sheet.widgets.length; i++) {
        const tempWidget = sheet.widgets[i]
        // TODO  - changeId
        // formattedSheet.widgets.lg.push({ id: tempWidget.id, x: tempWidget.sizeX, y: tempWidget.sizeY, h: 100, w: 100, i: cryptoRandomString({ length: 16, type: 'base64' }) })
        formattedSheet.widgets.lg.push({ id: tempWidget.id, h: 5, w: 10, x: 10, y: 10, i: cryptoRandomString({ length: 16, type: 'base64' }), moved: false })
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
