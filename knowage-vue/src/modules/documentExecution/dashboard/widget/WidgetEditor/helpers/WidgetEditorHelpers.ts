import { IWidget } from '../../../Dashboard'
import tableWidgetFunctions from './TableWidgetFunctions'
import cryptoRandomString from 'crypto-random-string'

export function createNewWidget() {
    const widget = {
        id: cryptoRandomString({ length: 16, type: 'base64' }),
        new: true,
        type: 'table',
        columns: [],
        conditionalStyles: [],
        datasets: [],
        interactions: [],
        theme: '',
        styles: {
            th: {
                enabled: true,
                'background-color': 'rgb(255, 255, 255)',
                color: 'rgb(137, 158, 175)',
                'justify-content': 'flex-start',
                'font-size': '14px',
                multiline: false,
                height: 25,
                'font-style': '',
                'font-weight': '',
                'font-family': ''
            }
        },
        settings: {
            rowThresholds: {
                enabled: false,
                list: []
            }
        },
        temp: {}
    } as any

    return widget
}

export function setWidgetModelTempProperty(widget: IWidget) {
    if (!widget.temp) widget.temp = {}
}

export function setWidgetModelFunctions(widget: IWidget) {
    if (widget.type === 'table') {
        if (!widget.settings.pagination) widget.settings.pagination = { enabled: false, itemsNumber: 0 }
        widget.functions = tableWidgetFunctions
    }
}