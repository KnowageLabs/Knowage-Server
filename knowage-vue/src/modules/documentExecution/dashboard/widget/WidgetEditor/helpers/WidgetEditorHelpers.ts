import { IWidget } from '../../../Dashboard'
import { formatTableWidgetForSave } from './tableWidget/TableWidgetBackendSaveHelper'
import { createNewTableWidgetSettings } from '../helpers/tableWidget/TableWidgetFunctions'
import { createNewSelectorWidgetSettings } from '../helpers/selectorWidget/SelectorWidgetFunctions'
import cryptoRandomString from 'crypto-random-string'
import deepcopy from 'deepcopy'

export function createNewWidget(type: string) {
    console.log(">>>>>>>> type: ", type)
    const widget = {
        id: cryptoRandomString({ length: 16, type: 'base64' }),
        new: true,
        type: type,
        dataset: null,
        columns: [],
        settings: {
        }

    } as IWidget

    createNewWidgetSettings(widget)

    return widget
}

const createNewWidgetSettings = (widget: IWidget) => {
    switch (widget.type) {
        case 'table':
            widget.settings = createNewTableWidgetSettings()
            break
        case 'selector':
            widget.settings = createNewSelectorWidgetSettings()
            break
    }
}


export function formatWidgetForSave(tempWidget: IWidget) {
    if (!tempWidget) return

    const widget = deepcopy(tempWidget)

    switch (widget.type) {
        case 'table': formatTableWidgetForSave(widget)
    }

    return widget
}

export function getRGBColorFromString(color: string) {
    const temp = color
        ?.trim()
        ?.substring(4, color.length - 1)
        ?.split(',')

    if (temp) {
        return { r: +temp[0], g: +temp[1], b: +temp[2] }
    } else return { r: 0, g: 0, b: 0 }
}