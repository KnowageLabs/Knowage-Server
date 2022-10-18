/**
 * ! this helper will get the input informations from the widget requests and create an hash that will be used as unique data request identifier.
 * ! When the same data will be requested the helper will get it from the indexedDB, new data will be requested to the BE
 * TODO: add the hash manager and the indexedDB manager (dexie?)
 */

import { AxiosResponse } from 'axios'
import { IDataset, ISelection, IWidget } from './Dashboard'

export const getData = (item) =>
    new Promise((resolve) => {
        setTimeout(() => {
            resolve({ item, ...new Date() })
        }, 1000)
    })

const formatSelectorModelForGet = (propWidget: IWidget, datasetLabel: string, initialCall: boolean, selections: ISelection[]) => {
    //TODO: strong type this
    //TODO: Make method that will merge associations and selections with dataToSend object.
    var dataToSend = {
        aggregations: {
            dataset: '',
            measures: [],
            categories: []
        },
        parameters: {},
        selections: initialCall ? {} : getFormattedSelections(selections),
        indexes: []
    } as any

    dataToSend.aggregations.dataset = datasetLabel
    // TODO - Uncomment filters
    // dataToSend.selections = getFilters(propWidget, datasetLabel)

    propWidget.columns.forEach((column) => {
        if (column.fieldType === 'MEASURE') {
            let measureToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, funct: column.aggregation, orderColumn: column.alias } as any
            column.formula ? (measureToPush.formula = column.formula) : ''
            dataToSend.aggregations.measures.push(measureToPush)
        } else {
            let attributeToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, orderType: '', funct: 'NONE' } as any
            attributeToPush.orderType = propWidget.settings.sortingOrder
            dataToSend.aggregations.categories.push(attributeToPush)
        }
    })

    return dataToSend
}

export const getSelectorWidgetData = async (widget: IWidget, datasets: IDataset[], $http: any, initialCall: boolean, selections: ISelection[]) => {
    console.log("_________________________ CAAAAAAAAAAALED FOR WIDGET: ", widget)
    var datasetIndex = datasets.findIndex((dataset: any) => widget.dataset === dataset.id.dsId)
    var selectedDataset = datasets[datasetIndex]

    if (selectedDataset) {
        var url = `2.0/datasets/${selectedDataset.label}/data?offset=-1&size=-1&nearRealtime=true`

        let postData = formatSelectorModelForGet(widget, selectedDataset.label, initialCall, selections)
        var tempResponse = null as any

        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData)
            .then((response: AxiosResponse<any>) => (tempResponse = response.data))
            .catch(() => { })
        return tempResponse
    }
}

const getFilters = (propWidget: IWidget, datasetLabel: string) => {
    var columns = propWidget.columns
    var activeFilters = {} as any

    columns.forEach((column) => {
        if (column.filter.enabled && column.filter.operator) {
            var filterData = { filterOperator: column.filter.operator, filterVals: [`('${column.filter.value}')`] }
            createNestedObject(activeFilters, [datasetLabel, column.columnName], filterData)
        }
    })

    return activeFilters
}

const createNestedObject = function (base, names, value) {
    var lastName = arguments.length === 3 ? names.pop() : false

    for (var i = 0; i < names.length; i++) {
        base = base[names[i]] = base[names[i]] || {}
    }
    if (lastName) base = base[lastName] = value

    return base
}

// TODO
const getFormattedSelections = (selections: ISelection[]) => {
    const formattedSelections = {}
    console.log("_________________________ SELECTIONS: ", selections)
    selections.forEach((selection: ISelection) => {
        const formattedFilterValues = selection.value.map((value: string | number) => "('" + value + "')")
        if (formattedSelections[selection.datasetLabel]) formattedSelections[selection.datasetLabel][selection.columnName] = formattedFilterValues
        else {
            const key = selection.columnName
            formattedSelections[selection.datasetLabel] = { [key]: formattedFilterValues }

        }
    })
    console.log(" _____________ FORMATTED SELECTIONS: ", formattedSelections)
    return formattedSelections
}