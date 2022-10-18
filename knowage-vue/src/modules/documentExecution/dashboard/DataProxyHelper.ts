/**
 * ! this helper will get the input informations from the widget requests and create an hash that will be used as unique data request identifier.
 * ! When the same data will be requested the helper will get it from the indexedDB, new data will be requested to the BE
 * TODO: add the hash manager and the indexedDB manager (dexie?)
 */

import { AxiosResponse } from 'axios'
import { IDataset, IWidget } from './Dashboard'

export const getData = (item) =>
    new Promise((resolve) => {
        setTimeout(() => {
            resolve({ item, ...new Date() })
        }, 1000)
    })

const formatSelectorModelForGet = (propWidget: IWidget, datasetLabel) => {
    //TODO: strong type this
    //TODO: Make method that will merge associations and selections with dataToSend object.
    var dataToSend = {
        aggregations: {
            dataset: '',
            measures: [],
            categories: []
        },
        parameters: {},
        selections: {},
        indexes: []
    } as any

    dataToSend.aggregations.dataset = datasetLabel

    propWidget.columns.forEach((column) => {
        if (column.fieldType === 'MEASURE') {
            let measureToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, funct: column.aggregation, orderColumn: column.alias } as any
            column.formula ? (measureToPush.formula = column.formula) : ''
            dataToSend.aggregations.measures.push(measureToPush)
        } else {
            let attributeToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, orderType: '', funct: 'NONE' } as any
            column.id === propWidget.settings.sortingColumn ? (attributeToPush.orderType = propWidget.settings.sortingOrder) : ''
            dataToSend.aggregations.categories.push(attributeToPush)
        }
    })

    return dataToSend
}

export const getSelectorWidgetData = async (widget: IWidget, datasets: IDataset[], $http: any) => {
    var datasetIndex = datasets.findIndex((dataset: any) => widget.dataset === dataset.id.dsId)
    var selectedDataset = datasets[datasetIndex]

    if (selectedDataset) {
        var url = `2.0/datasets/${selectedDataset.label}/data?offset=-1&size=-1&nearRealtime=true`

        let postData = formatSelectorModelForGet(widget, selectedDataset.label)
        var tempResponse = null as any

        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData)
            .then((response: AxiosResponse<any>) => (tempResponse = response.data))
            .catch(() => {})
        return tempResponse
    }
}
