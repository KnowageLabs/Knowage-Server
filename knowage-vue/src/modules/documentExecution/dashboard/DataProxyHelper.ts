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

export const getWidgetData = async (widget: IWidget, datasets: IDataset[], $http: any, initialCall: boolean, selections: ISelection[]) => {
    switch (widget.type) {
        case 'table':
            return await getTableWidgetData(widget, datasets, $http, initialCall, selections)
        case 'selector':
            return await getSelectorWidgetData(widget, datasets, $http, initialCall, selections)

        default:
            break
    }
}

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

    //summary rows - exclusive to table
    if (propWidget.type === 'table' && propWidget.settings.configuration.summaryRows.enabled) {
        dataToSend.summaryRow = getSummaryRow(propWidget)
    }

    propWidget.columns.forEach((column) => {
        if (column.fieldType === 'MEASURE') {
            let measureToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, funct: column.aggregation, orderColumn: column.alias } as any
            column.formula ? (measureToPush.formula = column.formula) : ''
            dataToSend.aggregations.measures.push(measureToPush)
        } else {
            let attributeToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, orderType: '', funct: 'NONE' } as any

            //sort logic - to be changed by other widgets
            if (propWidget.type === 'table') column.id === propWidget.settings.sortingColumn ? (attributeToPush.orderType = propWidget.settings.sortingOrder) : ''
            else attributeToPush.orderType = propWidget.settings.sortingOrder

            dataToSend.aggregations.categories.push(attributeToPush)
        }
    })

    return dataToSend
}

export const getSelectorWidgetData = async (widget: IWidget, datasets: IDataset[], $http: any, initialCall: boolean, selections: ISelection[]) => {
    var datasetIndex = datasets.findIndex((dataset: any) => widget.dataset === dataset.id.dsId)
    var selectedDataset = datasets[datasetIndex]

    if (selectedDataset) {
        var url = `2.0/datasets/${selectedDataset.label}/data?offset=-1&size=-1&nearRealtime=true`

        let postData = formatSelectorModelForGet(widget, selectedDataset.label, initialCall, selections)
        var tempResponse = null as any

        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData)
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                tempResponse.initialCall = initialCall
            })
            .catch(() => { })
        return tempResponse
    }
}

export const getTableWidgetData = async (widget: IWidget, datasets: IDataset[], $http: any, initialCall: boolean, selections: ISelection[]) => {
    var datasetIndex = datasets.findIndex((dataset: IDataset) => widget.dataset === dataset.id.dsId)
    var selectedDataset = datasets[datasetIndex] as any

    if (selectedDataset) {
        var url = ''
        if (widget.settings.pagination.enabled) {
            // url = `2.0/datasets/${selectedDataset.label}/data?offset=${pagination.offset}&size=${widget.settings.pagination.itemsNumber}&nearRealtime=true`
            url = `2.0/datasets/${selectedDataset.label}/data?offset=0&size=${widget.settings.pagination.properties.itemsNumber}&nearRealtime=true`
        } else url = `2.0/datasets/${selectedDataset.label}/data?offset=0&size=-1&nearRealtime=true`

        let postData = formatSelectorModelForGet(widget, selectedDataset.label, initialCall, selections)
        var tempResponse = null as any

        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData)
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                // pagination.totalItems = response.data.results
            })
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

const getSummaryRow = (propWidget: IWidget) => {
    var summaryArray = [] as any
    var columns = propWidget.columns
    for (var k in propWidget.settings.configuration.summaryRows.list) {
        var measures = [] as any
        if (columns) {
            for (var i = 0; i < columns.length; i++) {
                var col = columns[i]
                if (col.fieldType != 'ATTRIBUTE') {
                    var obj = {}
                    obj['id'] = col.columnName || col.alias
                    obj['alias'] = col.alias || col.alias
                    obj['funct'] = col.aggregation

                    if (col.formula) {
                        obj['formula'] = col.formula
                    } else obj['columnName'] = col.columnName

                    measures.push(obj)
                }
            }
        }
        var result = {} as any
        result['measures'] = measures
        result['dataset'] = propWidget.dataset
        summaryArray.push(result)
    }

    return summaryArray
}

// TODO
const getFormattedSelections = (selections: ISelection[]) => {
    const formattedSelections = {}
    selections?.forEach((selection: ISelection) => {
        const formattedFilterValues = selection.value.map((value: string | number) => "('" + value + "')")
        if (formattedSelections[selection.datasetLabel]) formattedSelections[selection.datasetLabel][selection.columnName] = formattedFilterValues
        else {
            const key = selection.columnName
            formattedSelections[selection.datasetLabel] = { [key]: formattedFilterValues }
        }
    })
    return formattedSelections
}
