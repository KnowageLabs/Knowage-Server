
import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'
import { formatTableWidget } from './TableWidgetHelpers'

export const formatModel = (model: any) => {
    console.log("FORMAT MODEL CALLED WITH: ", model)
    if (!model.sheets) return

    // TODO - id
    const formattedModel = {
        id: 1,
        widgets: [],
        version: model.knowageVersion,
        configuration: model.configuration,
        sheets: []
    } as any
    for (let i = 0; i < model.sheets.length; i++) {
        const formattedSheet = formatSheet(model.sheets[i], formattedModel)
        console.log("FORMATTED SHEET: ", formattedSheet)
        formattedModel.sheets.push(formattedSheet)
    }
    return formattedModel
}

const formatSheet = (sheet: any, formattedModel: any) => {
    console.log("SHEET: ", sheet)
    if (!sheet.widgets) return

    const formattedSheet = deepcopy(sheet)
    formattedSheet.widgets = { lg: [] }

    for (let i = 0; i < sheet.widgets.length; i++) {
        const tempWidget = sheet.widgets[i]
        // TODO  - changeId
        formattedSheet.widgets.lg.push({ id: tempWidget.id, x: tempWidget.sizeX, y: tempWidget.sizeY, h: 100, w: 100, i: cryptoRandomString({ length: 16, type: 'base64' }) })
        addWidgetToModel(tempWidget, formattedModel)
    }

    return formattedSheet
}

const addWidgetToModel = (widget: any, formattedModel: any) => {
    console.log("checkIfWidgetInModel: ", checkIfWidgetInModel(widget, formattedModel))
    if (checkIfWidgetInModel(widget, formattedModel)) return
    formattedModel.widgets.push(formatWidget(widget))
}

const checkIfWidgetInModel = (widget: any, formattedModel: any) => {
    let found = false;
    if (!formattedModel.widgets) return found
    for (let i = 0; i < formattedModel.widgets.length; i++) {
        if (formattedModel.widgets[i].id === widget.id) {
            found = true;
            break;
        }
    }

    return found;
}

export const formatWidget = (widget: any) => {
    console.log("FORMAT WIDGET FOR: ", widget)
    let formattedWidget = {} as any

    switch (widget.type) {
        case 'table':
            formattedWidget = formatTableWidget(widget)

    }

    return formattedWidget
}