/**
 * ! this helper will get the input informations from the widget requests and create an hash that will be used as unique data request identifier.
 * ! When the same data will be requested the helper will get it from the indexedDB, new data will be requested to the BE
 * TODO: add the hash manager and the indexedDB manager (dexie?)
 */

import { AxiosResponse } from 'axios'
import { IDataset, ISelection, IVariable, IWidget } from './Dashboard'
import { setDatasetInterval, clearDatasetInterval } from './helpers/datasetRefresh/DatasetRefreshHelpers'
import i18n from '@/App.i18n'
import store from '@/App.store.js'

const { t } = i18n.global
const mainStore = store()
const noAggregationsExistRegex = /\[kn-column=\'[a-zA-Z0-9\_\-\s]+\'(?:\s+row=\'\d+\')?(?!\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')(?:\s+precision=\'(?:\d)\')?(?:\s+format)?\s?\]/g
const limitRegex = /<[\s\w\=\"\'\-\[\]]*(?!limit=)"([\-\d]+)"[\s\w\=\"\'\-\[\]]*>/g
const rowsRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d+)\'){1}(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\s?\])/g

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
        case 'html':
            return await getHtmlWidgetData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
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
        } else {
            selectionsToSend[key] = filters[key]
        }
    })
}

const addFilterToSelection = (selection: any, filter: any) => {
    const filterColumnKeys = filter ? Object.keys(filter) : []
    filterColumnKeys.forEach((key: string) => {
        if (selection[key]) {
            selection[key].push(filter[key])
        } else {
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
                showGetDataError(error, selectedDataset.label)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })
        return tempResponse
    }
}

export const getHtmlWidgetData = async (widget: IWidget, datasets: IDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    var datasetIndex = datasets.findIndex((dataset: any) => widget.dataset === dataset.id.dsId)
    var selectedDataset = datasets[datasetIndex]

    console.log('GET HTML WIDGET DATA TRIGGERED')
    console.group('Console group example')
    console.log(widget)
    console.log(selectedDataset)
    console.groupEnd()

    if (selectedDataset && widget.settings.editor.html) {
        var html = widget.settings.editor.html
        var numOfRowsToGet = maxRow(widget)
        var url = `2.0/datasets/${selectedDataset.label}/data?offset=0&size=${numOfRowsToGet}&nearRealtime=true&limit=${numOfRowsToGet}`

        if (aggregationsExistInHtml(html)) {
            //TODO: AGGREGATION LOGIC OMFG
        } else {
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
                    showGetDataError(error, selectedDataset.label)
                })
                .finally(() => {
                    // TODO - uncomment when realtime dataset example is ready
                    // resetDatasetInterval(widget)
                })
            return tempResponse
        }
    }
}

const maxRow = (widgetModel) => {
    if (!widgetModel) return
    const str = widgetModel.settings.editor.css + widgetModel.settings.editor.html
    let tempMaxRow = 1
    const repeaters = str.replace(limitRegex, function (match: string, p1: any) {
        if (parseInt(p1) == -1) tempMaxRow = -1
        else if (p1 > tempMaxRow) tempMaxRow = parseInt(p1) + 1
    })
    const occurrencies = str.replace(rowsRegex, function (match: string, p1: any, p2: any) {
        if (p2 >= tempMaxRow) tempMaxRow = parseInt(p2) + 1
    })
    return tempMaxRow
}

const aggregationsExistInHtml = (html) => {
    return html.search(noAggregationsExistRegex) == -1
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
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })

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

const showGetDataError = (error: any, datasetLabel: string) => {
    let message = error.message
    if (error.message === '100') {
        message = t('dashboard.getDataError', { datasetLabel: datasetLabel })
    }
    mainStore.setError({ title: t('common.toast.errorTitle'), msg: message })
}

const resetDatasetInterval = (widget: IWidget) => {
    // TODO - set proper interval when realtime dataset example is ready
    if (widget.dataset || widget.dataset === 0) setDatasetInterval(widget.dataset as number, 10000)
}

export const getVariableData = async (variable: IVariable, datasets: IDataset[], $http: any) => {
    console.log('>>>> VARIABLE: ', variable)
    const selectedDataset = getVariableDatasetLabel(variable, datasets)
    if (!selectedDataset) return
    const url = `2.0/datasets/${selectedDataset.label}/data?offset=-1&size=-1&widgetName=undefined`
    const postData = { aggregations: { dataset: selectedDataset.label, measures: [], categories: [] }, parameters: {}, selections: {}, indexes: [] }
    let tempResponse = null as any
    await $http
        .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
        .then((response: AxiosResponse<any>) => (tempResponse = response.data))
        .catch((error: any) => {
            showGetDataError(error, selectedDataset.label)
        })
    return tempResponse
}

const getVariableDatasetLabel = (variable: IVariable, datasets: IDataset[]) => {
    var datasetIndex = datasets.findIndex((dataset: IDataset) => variable.dataset === dataset.id.dsId)
    return datasetIndex !== -1 ? datasets[datasetIndex] : null
}
