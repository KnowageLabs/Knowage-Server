import { IWidget, IWidgetColumn } from '../../../Dashboard'
import { formatTableWidgetForSave } from './TableWidgetFunctions'
import tableWidgetFunctions from './TableWidgetFunctions'
import cryptoRandomString from 'crypto-random-string'
import deepcopy from 'deepcopy'

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
            borders: true,
            border: {
                "border-top-left-radius": "",
                "border-top-right-radius": "",
                "border-bottom-left-radius": "",
                "border-bottom-right-radius": "",
                "border-color": "rgb(212, 212, 212)",
                "border-width": "1px",
                "border-style": "solid"

            },
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
            },
            tr: {
                height: 0
            }
        },
        settings: {
            alternateRows: {
                enabled: true,
                evenRowsColor: "rgb(228, 232, 236)",
                oddRowsColor: ""
            },
            indexColumn: false,
            multiselectable: false,
            multiselectablecolor: "",
            norows: {
                hide: false,
                message: ""
            },
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

export function formatWidgetColumnsForDisplay(widget: IWidget) {
    if (!widget || !widget.columns) return
    widget.columns.forEach((column: IWidgetColumn) => {
        if (!column.columnName?.startsWith('(')) column.columnName = '(' + column.columnName + ')'
    })
}

export function formatWidgetForSave(tempWidget: IWidget) {
    if (!tempWidget) return

    const widget = deepcopy(tempWidget)

    switch (widget.type) {
        case 'table': formatTableWidgetForSave(widget)
    }

    return widget
}

export function formatRGBColor(color: { r: string, g: string, b: string }) {

    return `rgb(${color.r}, ${color.g}, ${color.b})`
}