import { setWidgetModelFunctions } from '../helpers/WidgetEditorHelpers'

const mockedModel = {
    id: 'MbeScby/ztSqXsqy',
    type: 'table',
    columns: [
        {
            dataset: 186,
            name: '(MONTH_NAME)',
            alias: 'MONTH_NAME',
            type: 'java.lang.String',
            fieldType: 'ATTRIBUTE',
            aggregation: 'NONE',
            style: {
                hiddenColumn: false,
                'white-space': 'nowrap',
                tooltip: {
                    prefix: '',
                    suffix: '',
                    precision: 0
                },
                enableCustomHeaderTooltip: false,
                customHeaderTooltip: ''
            },
            enableTooltip: false,
            visType: ''
        },
        {
            dataset: 186,
            name: '(UNIT_SALES)',
            alias: 'UNIT_SALES',
            type: 'java.math.BigDecimal',
            fieldType: 'MEASURE',
            aggregation: 'MAX',
            style: {
                hiddenColumn: false,
                'white-space': 'nowrap',
                tooltip: {
                    prefix: 'prefix',
                    suffix: '',
                    precision: 2,
                    sufix: 'sufix'
                },
                enableCustomHeaderTooltip: false,
                customHeaderTooltip: ''
            },
            enableTooltip: true,
            visType: ''
        }
    ],
    conditionalStyles: [],
    datasets: [186],
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
        },
        pagination: {
            enabled: true,
            itemsNumber: 23
        }
    },
    temp: {
        selectedColumn: {
            dataset: 186,
            name: '(UNIT_SALES)',
            alias: 'UNIT_SALES',
            type: 'java.math.BigDecimal',
            fieldType: 'MEASURE',
            aggregation: 'MAX',
            style: {
                hiddenColumn: false,
                'white-space': 'nowrap',
                tooltip: {
                    prefix: 'prefix',
                    suffix: '',
                    precision: 2,
                    sufix: 'sufix'
                },
                enableCustomHeaderTooltip: true,
                customHeaderTooltip: ''
            },
            enableTooltip: true,
            visType: ''
        }
    },
    functions: {}
}
setWidgetModelFunctions(mockedModel)

export default mockedModel