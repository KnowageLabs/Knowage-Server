/* eslint-disable @typescript-eslint/no-unused-vars */
/**
 * ! this helper will get the input informations from the widget requests and create an hash that will be used as unique data request identifier.
 * ! When the same data will be requested the helper will get it from the indexedDB, new data will be requested to the BE
 * TODO: add the hash manager and the indexedDB manager (dexie?)
 */

import i18n from '@/App.i18n'
import deepcopy from 'deepcopy'
import store from '@/App.store.js'
import dashboardStore from '@/modules/documentExecution/dashboard/Dashboard.store'
import { AxiosResponse } from 'axios'
import { setDatasetInterval, clearDatasetInterval } from './helpers/datasetRefresh/DatasetRefreshHelpers'
import { aggregationRegex, aggregationsRegex, limitRegex, rowsRegex } from './helpers/common/DashboardRegexHelper'
import { IDataset, ISelection, IVariable, IWidget, IDashboardDataset, IDashboardDatasetDriver } from './Dashboard'

const { t } = i18n.global
const mainStore = store()
const dashStore = dashboardStore()

export const getData = (item) =>
    new Promise((resolve) => {
        setTimeout(() => {
            resolve({ item, ...new Date() })
        }, 1000)
    })

export const getWidgetData = async (dashboardId: any, widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    switch (widget.type) {
        case 'table':
            return await getTableWidgetData(dashboardId, widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'selector':
            return await getSelectorWidgetData(dashboardId, widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'html':
            return await getHtmlWidgetData(dashboardId, widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'text':
            return await getTextWidgetData(dashboardId, widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'highcharts':
            return await getHighchartsWidgetData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'chartJS':
            return await getPieChartData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'customchart':
            return await getCustomChartData(dashboardId, widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'static-pivot-table':
            return await getPivotData(dashboardId, widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'discovery':
            return await getDiscoveryChartData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'vega':
            return await getPieChartData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        default:
            break
    }
}

//#region ===================== Common Methods - Formatting Model, Drivers, Parameters, Selections Management ====================================================
const formatWidgetModelForGet = (dashboardId: any, propWidget: IWidget, dataset: IDashboardDataset, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const dataToSend = {
        aggregations: {
            dataset: '',
            measures: [],
            categories: []
        },
        parameters: {},
        selections: {},
        drivers: {},
        indexes: []
    } as any

    addSelectionsToData(dataToSend, propWidget, dataset.dsLabel, initialCall, selections, associativeResponseSelections)

    dataToSend.aggregations.dataset = dataset.dsLabel

    //summary rows - exclusive to table
    if (propWidget.type === 'table' && propWidget.settings.configuration.summaryRows.enabled) {
        dataToSend.summaryRow = getSummaryRow(propWidget)
    }

    propWidget.columns.forEach((column) => {
        if (column.fieldType === 'MEASURE') {
            const measureToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, funct: column.aggregation, orderColumn: column.alias, orderType: propWidget.settings?.sortingOrder } as any
            column.formula ? (measureToPush.formula = column.formula) : ''
            dataToSend.aggregations.measures.push(measureToPush)
        } else {
            const attributeToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, orderType: '', funct: 'NONE' } as any

            //sort logic - to be changed by other widgets
            if (propWidget.type === 'table' || propWidget.type === 'html' || propWidget.type === 'text') column.id === propWidget.settings.sortingColumn ? (attributeToPush.orderType = propWidget.settings.sortingOrder) : ''
            else attributeToPush.orderType = propWidget.settings.sortingOrder

            dataToSend.aggregations.categories.push(attributeToPush)
        }
    })

    if (dataset.drivers && dataset.drivers.length > 0) {
        dataset.drivers.forEach((driver: IDashboardDatasetDriver) => {
            dataToSend.drivers[`${driver.urlName}`] = driver.parameterValue
        })
    }

    if (dataset.parameters && dataset.parameters.length > 0) {
        const paramRegex = /[^\$P{]+(?=\})/
        dataset.parameters.forEach((param: any) => {
            const matched = paramRegex.exec(param.value)
            if (matched && matched[0]) {
                const documentDrivers = dashStore.dashboards[dashboardId].drivers
                for (let index = 0; index < documentDrivers.length; index++) {
                    const driver = documentDrivers[index]
                    if (driver.urlName == matched[0]) {
                        dataToSend.parameters[`${param.name}`] = driver.value
                    }
                }
            } else dataToSend.parameters[`${param.name}`] = param.value
        })
    }

    return dataToSend
}

const addSelectionsToData = (dataToSend: any, propWidget: IWidget, datasetLabel: string | undefined, initialCall: boolean, selections: ISelection[], associativeResponseSelections: any) => {
    if (associativeResponseSelections) {
        dataToSend.selections = associativeResponseSelections
    } else if (!initialCall) {
        dataToSend.selections = getFormattedSelections(selections)
    }
    if (datasetLabel) addFiltersToPostData(propWidget, dataToSend.selections, datasetLabel)
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
const getFilters = (propWidget: IWidget, datasetLabel: string) => {
    let columns = [] as any
    if (propWidget.type === 'static-pivot-table') {
        const modelFields = propWidget.fields
        columns = modelFields?.columns.concat(modelFields.rows, modelFields.data, modelFields.filters)
    } else columns = propWidget.columns

    const activeFilters = {} as any

    columns.forEach((column) => {
        if (column.filter.enabled && column.filter.operator) {
            const filterData = { filterOperator: column.filter.operator, filterVals: [`('${column.filter.value}')`] }
            createNestedObject(activeFilters, [datasetLabel, column.columnName], filterData)
        }
    })

    return activeFilters
}

const createNestedObject = function (base, names, value) {
    const lastName = arguments.length === 3 ? names.pop() : false

    for (let i = 0; i < names.length; i++) {
        base = base[names[i]] = base[names[i]] || {}
    }
    if (lastName) base = base[lastName] = value

    return base
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

const showGetDataError = (error: any, datasetLabel: string | undefined) => {
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
    const datasetIndex = datasets.findIndex((dataset: IDataset) => variable.dataset === dataset.id)
    return datasetIndex !== -1 ? datasets[datasetIndex] : null
}
//#endregion ================================================================================================

//#region ===================== Table Widget ====================================================
export const getTableWidgetData = async (dashboardId: any, widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const datasetIndex = datasets.findIndex((dataset: IDashboardDataset) => widget.dataset === dataset.id)
    const selectedDataset = datasets[datasetIndex]

    if (selectedDataset) {
        let url = ''
        const pagination = widget.settings.pagination
        if (pagination.enabled) {
            url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=${pagination.properties.offset}&size=${pagination.properties.itemsNumber}&nearRealtime=true`
        } else url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=0&size=-1&nearRealtime=true`

        const postData = formatWidgetModelForGet(dashboardId, widget, selectedDataset, initialCall, selections, associativeResponseSelections)
        let tempResponse = null as any

        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                if (pagination.enabled) widget.settings.pagination.properties.totalItems = response.data.results
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset.dsLabel)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })

        return tempResponse
    }
}

const getSummaryRow = (propWidget: IWidget) => {
    const summaryArray = [] as any
    const columns = propWidget.columns
    for (const k in propWidget.settings.configuration.summaryRows.list) {
        const measures = [] as any
        if (columns) {
            for (let i = 0; i < columns.length; i++) {
                const col = columns[i]
                if (col.fieldType != 'ATTRIBUTE') {
                    const obj = {}
                    obj['id'] = col.columnName || col.alias
                    obj['alias'] = col.alias || col.alias

                    if (propWidget.settings.configuration.summaryRows.list[k].aggregation == 'Columns Default Aggregation') obj['funct'] = col.aggregation
                    else obj['funct'] = propWidget.settings.configuration.summaryRows.list[k].aggregation || col.aggregation

                    if (col.formula) {
                        obj['formula'] = col.formula
                    } else obj['columnName'] = col.columnName

                    measures.push(obj)
                }
            }
        }
        const result = {} as any
        result['measures'] = measures
        result['dataset'] = propWidget.dataset
        summaryArray.push(result)
    }

    return summaryArray
}
//#endregion ================================================================================================

//#region ===================== Selector Widget ====================================================
export const getSelectorWidgetData = async (dashboardId: any, widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const datasetIndex = datasets.findIndex((dataset: any) => widget.dataset === dataset.id)
    const selectedDataset = datasets[datasetIndex]

    if (selectedDataset) {
        const url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=-1&size=-1&nearRealtime=true`

        const postData = formatWidgetModelForGet(dashboardId, widget, selectedDataset, initialCall, selections, associativeResponseSelections)
        let tempResponse = null as any

        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                tempResponse.initialCall = initialCall
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset.dsLabel)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })
        return tempResponse
    }
}
//#endregion ================================================================================================

//#region ===================== Text & HTML Widget ====================================================
export const getTextWidgetData = async (dashboardId: any, widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const datasetIndex = datasets.findIndex((dataset: any) => widget.dataset === dataset.id)
    const selectedDataset = datasets[datasetIndex]

    if (selectedDataset && widget.settings.editor.text) {
        const text = widget.settings.editor.text
        const numOfRowsToGet = maxRow(widget)
        const url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=0&size=${numOfRowsToGet}&nearRealtime=true&limit=${numOfRowsToGet}`

        const aggregationsModel = getAggregationsModel(widget, text, selectedDataset)
        let aggregationDataset = null as any
        if (aggregationsModel) {
            const aggregationsPostData = formatWidgetModelForGet(dashboardId, aggregationsModel, selectedDataset, initialCall, selections, associativeResponseSelections)
            await $http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, aggregationsPostData, { headers: { 'X-Disable-Errors': 'true' } })
                .then((response: AxiosResponse<any>) => {
                    aggregationDataset = response.data
                })
                .catch((error: any) => {
                    showGetDataError(error, selectedDataset.dsLabel)
                })
        }

        const postData = formatWidgetModelForGet(null, widget, selectedDataset, initialCall, selections, associativeResponseSelections)
        let tempResponse = null as any
        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                tempResponse.initialCall = initialCall
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset.dsLabel)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })

        return { tempResponse: tempResponse, aggregationDataset: aggregationDataset }
    }
}

export const getHtmlWidgetData = async (dashboardId: any, widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const datasetIndex = datasets.findIndex((dataset: any) => widget.dataset === dataset.id)
    const selectedDataset = datasets[datasetIndex]

    if (selectedDataset && widget.settings.editor.html) {
        const html = widget.settings.editor.html
        const numOfRowsToGet = maxRow(widget)
        const url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=0&size=${numOfRowsToGet}&nearRealtime=true&limit=${numOfRowsToGet}`

        const aggregationsModel = getAggregationsModel(widget, html, selectedDataset)
        let aggregationDataset = null as any
        if (aggregationsModel) {
            const aggregationsPostData = formatWidgetModelForGet(dashboardId, aggregationsModel, selectedDataset, initialCall, selections, associativeResponseSelections)
            await $http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, aggregationsPostData, { headers: { 'X-Disable-Errors': 'true' } })
                .then((response: AxiosResponse<any>) => {
                    aggregationDataset = response.data
                })
                .catch((error: any) => {
                    showGetDataError(error, selectedDataset.dsLabel)
                })
        }

        const postData = formatWidgetModelForGet(null, widget, selectedDataset, initialCall, selections, associativeResponseSelections)
        let tempResponse = null as any
        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                tempResponse.initialCall = initialCall
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset.dsLabel)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })

        return { tempResponse: tempResponse, aggregationDataset: aggregationDataset }
    }
}

const maxRow = (widgetModel) => {
    if (!widgetModel) return

    const str = widgetModel.type == 'html' ? widgetModel.settings.editor.css + widgetModel.settings.editor.html : widgetModel.settings.editor.text
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

const getAggregationsModel = (widgetModel, rawHtml, selectedDataset) => {
    const aggregationsReg = rawHtml.match(aggregationsRegex)
    if (aggregationsReg) {
        const modelToSend = deepcopy(widgetModel)
        const tempModel = deepcopy(widgetModel)
        delete modelToSend.settings
        modelToSend.columns = []

        for (const a in aggregationsReg) {
            const aggregationReg = aggregationRegex.exec(aggregationsReg[a])
            for (const m in tempModel.columns) {
                if (aggregationReg && aggregationReg[1] && tempModel.columns[m].columnName == aggregationReg[1]) {
                    tempModel.columns[m].alias = aggregationReg[1] + '_' + aggregationReg[3]
                    tempModel.columns[m].fieldType = 'MEASURE'
                    tempModel.columns[m].aggregation = aggregationReg[3]
                    let exists = false
                    for (const c in modelToSend.columns) {
                        if (modelToSend.columns[c].alias == aggregationReg[1] + '_' + aggregationReg[3]) exists = true
                    }
                    if (!exists) modelToSend.columns.push(deepcopy(tempModel.columns[m]))
                }
            }
        }
        return modelToSend
    } else return null
}

//#endregion ================================================================================================
export const getHighchartsWidgetData = async (widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const chartType = widget.settings.chartModel?.model?.chart.type
    switch (chartType) {
        case 'pie':
            return await getPieChartData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'gauge':
            return await getGaugeChartData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'activitygauge':
            return await getGaugeChartData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'solidgauge':
            return await getGaugeChartData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        case 'heatmap':
            return await getGaugeChartData(widget, datasets, $http, initialCall, selections, associativeResponseSelections)
        default:
            return ''
    }
}

//#region ===================== Chart Widget ====================================================
const getPieChartData = async (widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const datasetIndex = datasets.findIndex((dataset: IDashboardDataset) => widget.dataset === dataset.id)
    const selectedDataset = datasets[datasetIndex]

    const measureCheck = widget.columns.findIndex((column: any) => column.fieldType === 'MEASURE') != -1
    const categoryCheck = widget.columns.findIndex((column: any) => column.fieldType !== 'MEASURE') != -1

    if (selectedDataset && measureCheck && categoryCheck) {
        const url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=-1&size=-1&nearRealtime=true`

        const postData = formatChartWidgetForGet(widget, selectedDataset, initialCall, selections, associativeResponseSelections)
        let tempResponse = null as any

        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                tempResponse.initialCall = initialCall
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset.dsLabel)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })
        return tempResponse
    }
}

export const getGaugeChartData = async (widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const datasetIndex = datasets.findIndex((dataset: IDashboardDataset) => widget.dataset === dataset.id)
    const selectedDataset = datasets[datasetIndex]
    const measureCheck = widget.columns.findIndex((column: any) => column.fieldType === 'MEASURE') != -1

    if (selectedDataset && measureCheck) {
        const url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=-1&size=-1&nearRealtime=true`

        const postData = formatChartWidgetForGet(widget, selectedDataset, initialCall, selections, associativeResponseSelections)
        let tempResponse = null as any

        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                tempResponse.initialCall = initialCall
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset.dsLabel)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })
        return tempResponse
    }
}

const formatChartWidgetForGet = (propWidget: IWidget, dataset: IDashboardDataset, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const dataToSend = {
        aggregations: {
            dataset: '',
            measures: [],
            categories: []
        },
        parameters: {},
        selections: {},
        drivers: {},
        indexes: []
    } as any

    addSelectionsToData(dataToSend, propWidget, dataset.dsLabel, initialCall, selections, associativeResponseSelections)
    dataToSend.aggregations.dataset = dataset.dsLabel

    const chartType = propWidget.settings.chartModel?.model?.chart.type
    if (chartType == 'gauge' || chartType == 'activitygauge' || chartType == 'solidgauge' || chartType == 'heatmap') {
        propWidget.columns.forEach((measure) => {
            const measureToPush = { id: `${measure.alias}_${measure.aggregation}`, alias: `${measure.alias}_${measure.aggregation}`, columnName: measure.columnName, funct: measure.aggregation, orderColumn: measure.alias } as any
            measure.formula ? (measureToPush.formula = measure.formula) : ''
            dataToSend.aggregations.measures.push(measureToPush)
        })
    } else {
        //MEASURE LOGIC - will ALWAYS HAVE ONE MEASURE
        const measureIndex = propWidget.columns.findIndex((column: any) => column.fieldType === 'MEASURE')
        const measure = propWidget.columns[measureIndex]

        const measureToPush = { id: `${measure.alias}_${measure.aggregation}`, alias: `${measure.alias}_${measure.aggregation}`, columnName: measure.columnName, funct: measure.aggregation, orderColumn: measure.alias } as any
        measure.formula ? (measureToPush.formula = measure.formula) : ''
        dataToSend.aggregations.measures.push(measureToPush)

        //FIRST CATEGORY LOGIC - TODO: Make it grab the drilldown Category instead of the first one.
        const categoryIndex = propWidget.columns.findIndex((column: any) => column.fieldType !== 'MEASURE')
        const category = propWidget.columns[categoryIndex]

        const categoryToPush = { id: category.alias, alias: category.alias, columnName: category.columnName, orderType: '', funct: 'NONE' } as any
        dataToSend.aggregations.categories.push(categoryToPush)
    }

    if (dataset.drivers && dataset.drivers.length > 0) {
        dataset.drivers.forEach((driver: IDashboardDatasetDriver) => {
            dataToSend.drivers[`${driver.urlName}`] = driver.parameterValue
        })
    }

    return dataToSend
}
//#endregion ================================================================================================

//#region ===================== Custom Chart Widget ====================================================
const getCustomChartData = async (dashboardId, widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const datasetIndex = datasets.findIndex((dataset: IDashboardDataset) => widget.dataset === dataset.id)
    const selectedDataset = datasets[datasetIndex]

    // const measureCheck = widget.columns.findIndex((column: any) => column.fieldType === 'MEASURE') != -1
    // const categoryCheck = widget.columns.findIndex((column: any) => column.fieldType !== 'MEASURE') != -1

    // if (selectedDataset && measureCheck && categoryCheck) {
    if (selectedDataset && widget.settings.editor.html) {
        const url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=-1&size=-1&nearRealtime=true`

        const postData = formatWidgetModelForGet(dashboardId, widget, selectedDataset, initialCall, selections, associativeResponseSelections)
        let tempResponse = null as any

        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                tempResponse.initialCall = initialCall
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset.dsLabel)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })
        return tempResponse
    }
}
//#endregion ================================================================================================

//#region ===================== Discovery Widget ====================================================
const getDiscoveryChartData = async (widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const datasetIndex = datasets.findIndex((dataset: IDashboardDataset) => widget.dataset === dataset.id)
    const selectedDataset = datasets[datasetIndex]

    if (selectedDataset) {
        let url = ''
        if (!widget.settings.pagination) {
            const pagination = { enabled: true, properties: { offset: 0, itemsNumber: 10, totalItems: null } }
            widget.settings.pagination = pagination
        }

        url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=${widget.settings.pagination.properties.offset}&size=${widget.settings.pagination.properties.itemsNumber}&nearRealtime=true`

        const postData = formatDiscoveryModelForGet(widget, selectedDataset, initialCall, selections, associativeResponseSelections)
        let tempResponse = null as any

        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                if (widget.settings.pagination.enabled) widget.settings.pagination.properties.totalItems = response.data.results
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset.dsLabel)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })

        return tempResponse
    }
}

const formatDiscoveryModelForGet = (propWidget: IWidget, dataset: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const dataToSend = {
        aggregations: {
            dataset: '',
            measures: [],
            categories: []
        },
        parameters: {},
        selections: {},
        drivers: {},
        indexes: [],
        likeSelections: {}
    } as any

    dataToSend.likeSelections[dataset.dsLabel] = {}
    addSelectionsToData(dataToSend, propWidget, dataset.dsLabel, initialCall, selections, associativeResponseSelections)

    dataToSend.aggregations.dataset = dataset.dsLabel

    propWidget.columns.forEach((column) => {
        if (column.fieldType === 'MEASURE') {
            const measureToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, funct: 'NONE', orderColumn: column.alias, functColumn: column.alias, orderType: '' } as any
            column.formula ? (measureToPush.formula = column.formula) : ''
            dataToSend.aggregations.measures.push(measureToPush)
        } else {
            const attributeToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, orderType: '', funct: 'COUNT', functColumn: column.alias } as any

            //sort logic - to be changed by other widgets
            if (propWidget.type === 'table' || propWidget.type === 'html' || propWidget.type === 'text') column.id === propWidget.settings.sortingColumn ? (attributeToPush.orderType = propWidget.settings.sortingOrder) : ''
            else attributeToPush.orderType = propWidget.settings.sortingOrder

            attributeToPush.orderType = ''
            dataToSend.aggregations.categories.push(attributeToPush)
        }
    })

    if (dataset.drivers && dataset.drivers.length > 0) {
        dataset.drivers.forEach((driver: IDashboardDatasetDriver) => {
            dataToSend.drivers[`${driver.urlName}`] = driver.parameterValue
        })
    }

    const searchWordSettings = propWidget.settings.search
    if (searchWordSettings.enabled && searchWordSettings.searchWord && searchWordSettings.columns.length > 0) {
        const searchPropName = searchWordSettings.columns.join(',')
        dataToSend.likeSelections[dataset.dsLabel][searchPropName] = searchWordSettings.searchWord.trim()
    }

    const facetSearchParams = propWidget.settings.search.facetSearchParams
    if (facetSearchParams) {
        const facetKeys = Object.keys(facetSearchParams)
        if (facetKeys.length > 0) {
            facetKeys.forEach((facetName) => {
                dataToSend.likeSelections[dataset.dsLabel][facetName] = facetSearchParams[facetName][0]
            })
        }
    }

    return dataToSend
}
//#endregion ================================================================================================

//#region ===================== Pivot Widget ====================================================
export const getPivotData = async (dashboardId: any, widget: IWidget, datasets: IDashboardDataset[], $http: any, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const datasetIndex = datasets.findIndex((dataset: IDashboardDataset) => widget.dataset === dataset.id)
    const selectedDataset = datasets[datasetIndex]

    if (selectedDataset && hasFields(widget)) {
        const url = `2.0/datasets/${selectedDataset.dsLabel}/data?offset=-1&size=-1&nearRealtime=true`

        const postData = formatPivotModelForGet(dashboardId, widget, selectedDataset, initialCall, selections, associativeResponseSelections)
        let tempResponse = null as any

        if (widget.dataset || widget.dataset === 0) clearDatasetInterval(widget.dataset)
        await $http
            .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData, { headers: { 'X-Disable-Errors': 'true' } })
            .then((response: AxiosResponse<any>) => {
                tempResponse = response.data
                tempResponse.initialCall = initialCall
            })
            .catch((error: any) => {
                showGetDataError(error, selectedDataset.dsLabel)
            })
            .finally(() => {
                // TODO - uncomment when realtime dataset example is ready
                // resetDatasetInterval(widget)
            })
        return tempResponse
    }
}

const hasFields = (propWidget: IWidget) => {
    const fields = propWidget.fields || ({} as any)

    if (fields.columns.length > 0 && fields.rows.length > 0 && fields.data.length > 0) return true
    else return false
}

const formatPivotModelForGet = (dashboardId: any, propWidget: IWidget, dataset: IDashboardDataset, initialCall: boolean, selections: ISelection[], associativeResponseSelections?: any) => {
    const dataToSend = {
        aggregations: {
            dataset: '',
            measures: [],
            categories: []
        },
        parameters: {},
        selections: {},
        drivers: {},
        indexes: []
    } as any

    addSelectionsToData(dataToSend, propWidget, dataset.dsLabel, initialCall, selections, associativeResponseSelections)

    dataToSend.aggregations.dataset = dataset.dsLabel

    for (const fieldsName in propWidget.fields) {
        const fields = propWidget.fields[fieldsName]
        fields.forEach((field) => {
            if (field.fieldType === 'MEASURE') {
                const measureToPush = { id: field.alias, alias: field.alias, columnName: field.columnName, funct: field.aggregation, orderColumn: field.alias } as any
                field.formula ? (measureToPush.formula = field.formula) : ''
                dataToSend.aggregations.measures.push(measureToPush)
            } else {
                const attributeToPush = { id: field.alias, alias: field.alias, columnName: field.columnName, orderType: '', funct: 'NONE' } as any

                //TODO: do we need sorting on BE???
                // if (propWidget.type === 'table' || propWidget.type === 'html' || propWidget.type === 'text') field.id === propWidget.settings.sortingColumn ? (attributeToPush.orderType = propWidget.settings.sortingOrder) : ''
                // else attributeToPush.orderType = propWidget.settings.sortingOrder

                dataToSend.aggregations.categories.push(attributeToPush)
            }
        })
    }

    if (dataset.drivers && dataset.drivers.length > 0) {
        dataset.drivers.forEach((driver: IDashboardDatasetDriver) => {
            dataToSend.drivers[`${driver.urlName}`] = driver.parameterValue
        })
    }

    if (dataset.parameters && dataset.parameters.length > 0) {
        const paramRegex = /[^\$P{]+(?=\})/
        dataset.parameters.forEach((param: any) => {
            const matched = paramRegex.exec(param.value)
            if (matched && matched[0]) {
                const documentDrivers = dashStore.dashboards[dashboardId].drivers
                for (let index = 0; index < documentDrivers.length; index++) {
                    const driver = documentDrivers[index]
                    if (driver.urlName == matched[0]) {
                        dataToSend.parameters[`${param.name}`] = driver.value
                    }
                }
            } else dataToSend.parameters[`${param.name}`] = param.value
        })
    }

    return dataToSend
}
//#endregion ================================================================================================
