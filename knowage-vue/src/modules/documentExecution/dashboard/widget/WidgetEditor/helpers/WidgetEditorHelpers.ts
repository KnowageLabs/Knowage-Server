import tableWidgetFunctions from './TableWidgetFunctions'

export function createNewWidget() {
    const widget = {
        type: 'tableWidget',
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
    if (widget.type === 'tableWidget') {
        widget.settings.pagination = { enabled: false, itemsNumber: 0 }
        widget.functions = tableWidgetFunctions
    }
    return widget
}