/**
 * ! this helper will get the input informations from the widget requests and create an hash that will be used as unique data request identifier.
 * ! When the same data will be requested the helper will get it from the indexedDB, new data will be requested to the BE
 * TODO: add the hash manager and the indexedDB manager (dexie?)
 */

import { AxiosResponse } from 'axios'
import { IDataset, ISelection, IWidget } from './Dashboard'
import { setDatasetInterval, clearDatasetInterval } from './helpers/datasetRefresh/DatasetRefreshHelpers'
import i18n from '@/App.i18n'
import store from '@/App.store.js'

const { t } = i18n.global
const mainStore = store()

export const getData = (item) =>
    new Promise((resolve) => {
        setTimeout(() => {
            resolve({ item, ...new Date() })
        }, 1000)
    })

export const getWidgetData = async (widget: IWidget, datasets: IDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    switch (widget.type) {
        case 'table':
            return await getTableWidgetData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'selector':
            return await getSelectorWidgetData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        default:
            break
    }
}

const formatSelectorModelForGet = (propWidget: IWidget, datasetLabel: string, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
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

    addSelectionsToData(dataToSend, propWidget, datasetLabel, initialCall, selections, associativeResponseSelections)

    dataToSend.aggregations.dataset = datasetLabel

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

const addSelectionsToData = (dataToSend: any, propWidget: IWidget, datasetLabel: string, initialCall: boolean, selections: ISelection[], associativeResponseSelections: any) => {
    if (associativeResponseSelections) {
        dataToSend.selections = associativeResponseSelections
    } else if (!initialCall) {
        dataToSend.selections = getFormattedSelections(selections)
    }
    addFiltersToPostData(propWidget, dataToSend.selections, datasetLabel)
}

const addFiltersToPostData = (propWidget: IWidget, selectionsToSend: any, datasetLabel: string) => {
    const filters = getFilters(propWidget, datasetLabel)
    const filterKeys = filters ? Object.keys(filters) : []
    filterKeys.forEach((key: string) => {
        if (selectionsToSend[key]) {
            addFilterToSelection(selectionsToSend[key], filters[key])
        }
        else {
            selectionsToSend[key] = filters[key]
        }
    })
}

const addFilterToSelection = (selection: any, filter: any) => {
    const filterColumnKeys = filter ? Object.keys(filter) : []
    filterColumnKeys.forEach((key: string) => {
        if (selection[key]) {
            selection[key].push(filter[key])
        }
        else {
            selection[key] = filter[key]
        }
    })
}

export const getSelectorWidgetData = async (widget: IWidget, datasets: IDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    var datasetIndex = datasets.findIndex((dataset: any) => widget.dataset === dataset.id.dsId)
    var selectedDataset = datasets[datasetIndex]

    if (selectedDataset) {
        var url = `2.0/datasets/${selectedDataset.label}/data?offset=-1&size=-1&nearRealtime=true`

        let postData = formatSelectorModelForGet(widget, selectedDataset.label, initialCall, selections, associativeResponseSelections)
        var tempResponse = null as any

        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                tempResponse.initialCall = initialCall
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset)
            })
            .finally(() => {
                if (widget.dataset || widget.dataset === 0) setDatasetInterval(widget.dataset as number, 10000)
            }) // TODO - SET PROPER INTERVAL
        return tempResponse
    }
}

export const getTableWidgetData = async (widget: IWidget, datasets: IDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    var datasetIndex = datasets.findIndex((dataset: IDataset) => widget.dataset === dataset.id.dsId)
    var selectedDataset = datasets[datasetIndex] as any

    if (selectedDataset) {
        var url = ''
        let pagination = widget.settings.pagination
        if (pagination.enabled) {
            url = `2.0/datasets/${selectedDataset.label}/data?offset=${pagination.properties.offset}&size=${pagination.properties.itemsNumber}&nearRealtime=true`
        } else url = `2.0/datasets/${selectedDataset.label}/data?offset=0&size=-1&nearRealtime=true`

        let postData = formatSelectorModelForGet(widget, selectedDataset.label, initialCall, selections, associativeResponseSelections)
        var tempResponse = null as any

        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                if (pagination.enabled) widget.settings.pagination.properties.totalItems = response.data.results
                // pagination.totalItems = response.data.results
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset)
            })
            .finally(() => {
                if (widget.dataset || widget.dataset === 0) setDatasetInterval(widget.dataset as number, 10000)
            }) // TODO - SET PROPER INTERVAL

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

const showGetDataError = (error: any, selectedDataset: IDataset) => {
    let message = error.message
    if (error.message === '100') {
        message = t('dashboard.getDataError', { datasetLabel: selectedDataset.label })
    }
    mainStore.setError({ title: t('common.toast.errorTitle'), msg: message })
}