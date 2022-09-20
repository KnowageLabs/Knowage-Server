import { IWidget } from '../../../Dashboard'
import { formatTableWidgetForSave } from './tableWidget/TableWidgetFunctions'
import tableWidgetFunctions from './tableWidget/TableWidgetFunctions'
import cryptoRandomString from 'crypto-random-string'
import deepcopy from 'deepcopy'

export function createNewWidget() {
    const widget = {
        id: cryptoRandomString({ length: 16, type: 'base64' }),
        new: true,
        type: 'table',
        dataset: null, // TODO - HARCODED
        columns: [],
        settings: {
            sortingColumn: '',
            sortingOrder: '',
            updatable: true,
            clickable: true,
            conditionalStyles: [],
            configuration: {
                columnGroups: {
                    enabled: false,
                    groups: []
                },
                exports: {
                    pdf: {
                        enabled: false,
                        custom: {
                            height: 0,
                            width: 0,
                            enabled: false
                        },
                        a4landscape: false,
                        a4portrait: false
                    },
                    showExcelExport: false,
                    showScreenshot: false
                },
                headers: {
                    enabled: false, enabledMultiline: false, custom: { enabled: false, rules: [] }
                },
                rows: {
                    indexColumn: false,
                    rowSpan: {
                        enabled: false,
                        column: ''
                    }
                },
                summaryRows: {
                    enabled: false,
                    list: [],
                    style: { pinnedOnly: false }
                },
                customMessages: {
                    hideNoRowsMessage: false,
                    noRowsMessage: ''
                }
            },
            interactions: {
                crosssNavigation: {
                    enabled: false,
                    type: '',
                    column: '',
                    name: '',
                    parameters: []
                },
                link: {
                    enabled: false,
                    links: []
                },
                preview: {
                    enabled: false,
                    type: '',
                    dataset: -1,
                    parameters: [],
                    directDownload: false
                },
                selection: {
                    enabled: false,
                    modalColumn: '',
                    multiselection: {
                        enabled: false,
                        properties: {
                            "background-color": '',
                            color: ''
                        }
                    }
                }
            },
            pagination: { enabled: false, itemsNumber: 0 },
            style: {
                borders: {
                    enabled: false,
                    properties: {
                        "border-bottom-left-radius": "",
                        "border-bottom-right-radius": "",
                        "border-style": "",
                        "border-top-left-radius": "",
                        "border-top-right-radius": "",
                        "border-width": "",
                        "border-color": "rgb(212, 212, 212)"
                    }
                },
                columns: [{
                    target: 'all',
                    properties: {
                        "background-color": '',
                        color: '',
                        "justify-content": '',
                        "font-size": '',
                        "font-family": '',
                        "font-style": '',
                        "font-weight": ''
                    }
                }],
                columnGroups: [{
                    target: 'all',
                    properties: {
                        "background-color": '',
                        color: '',
                        "justify-content": '',
                        "font-size": '',
                        "font-family": '',
                        "font-style": '',
                        "font-weight": ''
                    }
                }],
                headers: {
                    height: 25,
                    properties: {
                        "background-color": "rgb(137, 158, 175)",
                        color: 'rgb(255, 255, 255)',
                        "justify-content": 'center',
                        "font-size": "14px",
                        "font-family": "",
                        "font-style": "normal",
                        "font-weight": "",
                    }
                },
                padding: {
                    enabled: false,
                    properties: {
                        "padding-top": '',
                        "padding-left": '',
                        "padding-bottom": '',
                        "padding-right": '',
                        unlinked: false
                    }
                },
                rows: {
                    height: 0,
                    multiselectable: false,
                    selectionColor: '',
                    alternatedRows: {
                        enabled: false,
                        evenBackgroundColor: 'rgb(228, 232, 236)',
                        oddBackgroundColor: ''

                    }
                },
                shadows: {
                    enabled: false,
                    properties: {
                        "box-shadow": '',
                        "backgroundColor": ''
                    }
                },
                summary: {
                    "background-color": "",
                    "color": "",
                    "font-family": "",
                    "font-size": "",
                    "font-style": "",
                    "font-weight": "",
                    "justify-content": ""
                }
            },
            tooltips: [{
                target: 'all',
                enabled: false,
                prefix: '',
                suffix: '',
                precision: 0,
                header: {
                    enabled: false,
                    text: ''
                }
            }],
            visualization: {
                types: [{
                    target: 'all',
                    type: 'Text',
                    prefix: '',
                    suffix: '',
                    pinned: '',
                }],
                visibilityConditions: []
            },
            responsive: {
                xs: true,
                sm: true,
                md: true,
                lg: true,
                xl: true
            }

        }

    } as IWidget

    return widget
}



export function setWidgetModelFunctions(widget: IWidget) {
    if (widget.type === 'table') {
        if (!widget.settings.pagination) widget.settings.pagination = { enabled: false, itemsNumber: 0 }
        widget.functions = tableWidgetFunctions
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

export function formatRGBColor(color: { r: string, g: string, b: string }) {

    return `rgb(${color.r}, ${color.g}, ${color.b})`
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
